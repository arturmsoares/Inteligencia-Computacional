# 🧬 Algoritmo Genético - Gerador de Escalas Médicas

Documentação e anotações auxiliares do desenvolvimento de um Algoritmo Genético para otimização de escalas de atendimento médico.

---

## 🔹 Etapa 0: Representação do Cromossomo

Antes de qualquer código, precisamos definir como o AG vai "enxergar" o problema. **Essa é a decisão mais importante.**

### 📘 Conceito

Cada indivíduo representa uma escala semanal completa. Precisamos cobrir:
- **3 unidades × 7 dias × 3 turnos = 63 slots**
- Cada slot precisa ter **3 médicos alocados**
- Portanto: **63 slots × 3 médicos = 189 genes por cromossomo**

### 💻 Visualização da Estrutura

```
cromossomo[u][d][t][m]
            │   │  │  └─ médico 0, 1 ou 2 (índice de 0 a 24)
            │   │  └──── turno: 0=Manhã, 1=Tarde, 2=Noite
            │   └─────── dia: 0 a 6 (seg a dom)
            └─────────── unidade: 0, 1 ou 2
```

**Na prática**, vamos linearizar isso num vetor de 189 inteiros:

```java
int[] cromossomo = new int[189];
// índice = (unidade * 63) + (dia * 9) + (turno * 3) + posicaoMedico

gene = (u * 63) + (d * 9) + (t * 3)
//      │            │          │
//      │            │          └─ pula 3 genes por turno
//      │            └──────────── pula 9 genes por dia (3 turnos × 3 médicos)
//      └───────────────────────── pula 63 genes por unidade (7 dias × 9)
```

Para reverter (índice → coordenadas):

```java
int unidade       = indice / 63;
int resto1        = indice % 63;
int dia           = resto1 / 9;
int resto2        = resto1 % 9;
int turno         = resto2 / 3;
int posicaoMedico = resto2 % 3;
```

### 🧠 Por que inteiro e não String como no exemplo?

Cada gene é simplesmente o índice do médico **(0 a 24)** — mais simples e eficiente.

### ❓ Mapeamento de Especialidades

Existem 25 médicos. Cada especialidade tem 5 médicos:

```
Médicos  0– 4 → Clínica Geral
Médicos  5– 9 → Pediatria
Médicos 10–14 → Ginecologia
Médicos 15–19 → Ortopedia
Médicos 20–24 → Cardiologia
```

---

## 🔧 Estrutura do Projeto

### Componentes Principais

#### `Algoritmo.java`

Classe responsável por implementar o algoritmo genético:

| Campo | Tipo | Visibilidade | Descrição |
|-------|------|-------------|-----------|
| `tamPop` | `int` | private | Tamanho da população |
| `maxGen` | `int` | private | Número máximo de gerações |
| `pc` | `double` | private | Probabilidade de cruzamento |
| `pm` | `double` | private | Probabilidade de mutação |
| `pop` | `int[][]` | public | População atual — cada linha é um cromossomo de 189 genes |
| `popFilhos` | `int[][]` | private | População de filhos gerada a cada geração |
| `notas` | `double[]` | public | Fitness de cada indivíduo — quanto menor, melhor |
| `conflitos` | `int[]` | public | Marca genes com conflito — público para acesso pela `Interface.java` |

#### `Interface.java`

Interface gráfica em Java Swing. Permite configurar parâmetros, executar o AG e visualizar os resultados.

| Componente | Descrição |
|-----------|-----------|
| Painel de parâmetros | Campos para `tamPop`, `maxGen`, `pc`, `pm` |
| Log de execução | Exibe progresso geração a geração com cores |
| Tabela de escala | Exibe a melhor escala encontrada (Unidade × Dia × Turno × Médicos) |
| Coloração de conflitos | Células com violações são destacadas em vermelho claro |

---

## 📋 Fases do Desenvolvimento

### ✅ Etapa 1: População Inicial — `popInicial()`

Gera cromossomos aleatórios. Cada turno recebe 3 médicos sorteados entre os 25 disponíveis (índices 0–24).

> ⚠️ A restrição de pelo menos 1 Clínica Geral por turno **não é garantida na geração inicial** — ela é penalizada na avaliação. Isso é intencional: o AG aprende a respeitá-la ao longo das gerações.

```java
pop[ind][gene++] = rand.nextInt(25); // médico 1 do turno
pop[ind][gene++] = rand.nextInt(25); // médico 2 do turno
pop[ind][gene++] = rand.nextInt(25); // médico 3 do turno
```

---

### ✅ Etapa 2: Avaliação — `avaliacao()`

Calcula o fitness de cada indivíduo somando penalizações. Menor nota = melhor escala.

```java
notas[ind] += penalizaRepetidos(ind);
notas[ind] += penalizaClinicaGeral(ind);
notas[ind] += penalizaCargaHoraria(ind);
notas[ind] += penalizaConsecutivos(ind);
```

#### Penalizações implementadas

| Método | Violação detectada | Peso |
|--------|--------------------|------|
| `penalizaRepetidos` | Médico repetido no mesmo turno | 3 |
| `penalizaClinicaGeral` | Turno sem nenhum médico de Clínica Geral (0–4) | 1 |
| `penalizaCargaHoraria` | Médico com mais de 5 turnos semanais (> 40h) | 2 |
| `penalizaConsecutivos` | Médico em dois turnos seguidos (Manhã→Tarde, Tarde→Noite, Noite→Manhã do dia seguinte) | 2 |

> 💡 Cada turno equivale a 8h. Máximo de 40h/semana = máximo de 5 turnos por médico.

#### Verificação de turnos consecutivos

```
Para cada médico:
  Para cada dia:
    ├── Manhã → Tarde     (t=0 → t=1)
    ├── Tarde → Noite     (t=1 → t=2)
    └── Noite → Manhã     (dia d, t=2 → dia d+1, t=0) — verificado se d < 6
```

> O `if (d < 6)` não cria exceção à regra — apenas protege contra acesso a índice inexistente no último dia da semana.

---

### ✅ Etapa 3: Ordenação — `ordenacao()`

Ordena a população do menor para o maior fitness (bubble sort). Nota e indivíduo são **sempre trocados juntos** para manter a correspondência correta.

Após a ordenação: `pop[0]` = melhor indivíduo da geração.

---

### ✅ Etapa 4: Seleção — `selecao()` / `torneio()`

Seleciona dois pais via **seleção por torneio**: sorteia `k` candidatos aleatórios da população inteira e o melhor entre eles vira pai. Ambos os pais passam pelo mesmo processo de forma independente.

```java
pais[0] = torneio(3);
pais[1] = torneio(3);
```

O parâmetro `k` controla a pressão seletiva:

| k | Comportamento |
|---|---------------|
| 2 | Pressão baixa — mais diversidade |
| 3 | Equilíbrio — valor atual |
| 5+ | Pressão alta — converge mais rápido |

> Qualquer indivíduo tem chance de ser selecionado, mas os melhores ganham mais torneios — diversidade e pressão seletiva coexistem.

> ⚠️ `.clone()` é obrigatório para evitar que modificações no pai alterem a população original.

---

### ✅ Etapa 5: Cruzamento — `cruzamento()`

Utiliza **cruzamento uniforme**: cada gene decide individualmente de qual pai herdar, com 50% de chance para cada lado.

```java
if (num.nextDouble() < 0.5) {
    filhos[0][j] = pais[0][j];
    filhos[1][j] = pais[1][j];
} else {
    filhos[0][j] = pais[1][j];
    filhos[1][j] = pais[0][j];
}
```

> Ao contrário do cruzamento de ponto de corte (que transfere blocos contínuos), o cruzamento uniforme permite combinar genes de regiões distintas do cromossomo — por exemplo, um bom turno de terça do pai 0 com um bom turno de sexta do pai 1.

> Se `nextDouble() >= pc`: filhos são cópias dos pais (sem cruzamento).

---

### ✅ Etapa 6: Mutação — `mutacao()`

Aplica **mutação por reinserção aleatória**: para cada turno de cada filho, com probabilidade `pm`, substitui um gene por um médico sorteado aleatoriamente entre os 25 disponíveis.

```java
for (int i = 0; i < 189; i += 3) { // para cada turno
    if (pos.nextDouble() < pm) {
        int vagaAleatoria = i + pos.nextInt(3);
        filhos[k][vagaAleatoria] = pos.nextInt(25); // médico completamente novo
    }
}
```

> A verificação de `pm` ocorre **dentro** do loop de turnos — cada turno tem chance independente de sofrer mutação. Com `pm = 0.02` e 63 turnos por filho, em média ~1,26 turnos mutam por filho por geração.

> Diferente do swap (que apenas troca médicos já presentes), a reinserção introduz valores genuinamente novos no cromossomo, evitando que a população fique presa em ótimos locais.

---

### ✅ Etapa 7: Loop Principal — `aG()`

Orquestra todas as etapas em loop até atingir o número máximo de gerações.

```
popInicial()
  └─ do {
       avaliacao()
       ordenacao()
       registro()         ← salva melhor se notas[0] < melhorNota
       do {
           selecao()
           cruzamento()
           mutacao()
           insereFilhos()
       } while (filhos < tamPop)
       pop = popFilhos
     } while (contGen < maxGen)
```

---

### ✅ Etapa 8: Registro — `registro()`

Salva o melhor indivíduo e marca os genes com conflito para visualização na interface.

```java
// chamado apenas quando notas[0] < melhorNota (verificado no aG())
melhorIndividuo[j] = pop[0][j];
conflitos[j] = 0;  // zera antes de reanalisar
```

Tipos de conflito marcados:

| Conflito | Condição |
|---------|---------|
| Médico repetido no turno | `m1 == m2 \|\| m1 == m3 \|\| m2 == m3` |
| Turno sem Clínica Geral | `m1 > 4 && m2 > 4 && m3 > 4` |

> ⚠️ O `if (notas[0] < melhorNota)` foi **removido do interior do `registro()`** — a verificação já ocorre no `aG()` antes da chamada, evitando condição redundante que nunca seria verdadeira.

---

## 🎯 Restrições do Problema

### Restrições Duras (penalizadas com peso maior)

| Restrição | Peso |
|-----------|------|
| Médico repetido no mesmo turno | 3 |
| Turnos consecutivos para o mesmo médico | 2 |
| Excesso de carga horária (> 40h/semana) | 2 |

### Restrições Suaves (penalizadas com peso menor)

| Restrição | Peso |
|-----------|------|
| Turno sem Clínica Geral | 1 |

---

## 📊 Função de Fitness

```
fitness = (repetidos × 3) + (sem_CG × 1) + (excesso_horas × 2) + (consecutivos × 2)
```

`fitness = 0` representa a escala ideal — sem nenhuma violação.

---

## 📚 Referências de Estrutura de Índice

Para converter coordenadas `(u, d, t)` para índice linear:

```java
int gene = (u * 63) + (d * 9) + (t * 3);
// gene+0 → médico 1 do turno
// gene+1 → médico 2 do turno
// gene+2 → médico 3 do turno
```

---

## 🚀 Como Executar

### Compilação
```bash
javac geradorescala/*.java
```

### Execução via Interface Gráfica
```bash
java geradorescala.Interface
```

### Execução via Terminal
```bash
java geradorescala.Main
```

---

## 📝 Parâmetros Recomendados

| Parâmetro | Valor sugerido | Descrição |
|-----------|---------------|-----------|
| `tamPop` | 100–200 | Tamanho da população |
| `maxGen` | 500–1000 | Número máximo de gerações |
| `pc` | 0.80–0.95 | Probabilidade de cruzamento |
| `pm` | 0.01–0.05 | Probabilidade de mutação por turno |