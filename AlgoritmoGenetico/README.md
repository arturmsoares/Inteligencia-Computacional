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

Gera cromossomos aleatórios. **Cada turno recebe 3 médicos DIFERENTES**, garantindo que não haja repetição de médicos no mesmo turno/dia/unidade desde o início.

```java
boolean[] usado = new boolean[25];
int gene = (u * 63) + (d * 9) + (t * 3);

for (int vaga = 0; vaga < 3; vaga++) {
    int medico;
    do {
        medico = rand.nextInt(25);
    } while (usado[medico]);  // Garante médicos diferentes
    
    pop[ind][gene + vaga] = medico;
    usado[medico] = true;
}
```

**Benefício**: Elimina a penalização `penalizaRepetidos()` desde a geração inicial.

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
| `penalizaClinicaGeral` | Turno sem nenhum médico de Clínica Geral (0–4) | 5 |
| `penalizaCargaHoraria` | Médico com mais de 5 turnos semanais (> 40h) | 2 |
| `penalizaConsecutivos` | Médico em dois turnos seguidos (Manhã→Tarde, Tarde→Noite, Noite→Manhã do dia seguinte) | 2 |
| `penalizaMultiplasUnidades` | Médico em múltiplas unidades no mesmo turno/dia | 5 |

> ✅ `penalizaRepetidos()` foi removida — garantida na população inicial.
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

Seleciona dois pais via **seleção por torneio**: sorteia `k` candidatos aleatórios da população inteira e o melhor entre eles vira pai.

```java
pais[0] = torneio(7);  // Torneio com 7 candidatos (pressão seletiva)
pais[1] = torneio(7);
```

| k | Comportamento |
|---|---------------|
| 3-5 | Equilíbrio — mais diversidade |
| 7-10 | **Pressão alta — converge mais rápido** |
| 15+ | Muito rigoroso — convergência prematura |

> Qualquer indivíduo tem chance de ser selecionado, mas os melhores ganham mais torneios — diversidade e pressão seletiva coexistem.

> ⚠️ `.clone()` é obrigatório para evitar que modificações no pai alterem a população original.

---

### ✅ Etapa 5: Cruzamento — `cruzamento()`

Utiliza **cruzamento por turno**: cada turno (3 genes) é herdado integralmente de um dos pais (50% chance cada), preservando a validade dos cromossomos.

```java
for (int i = 0; i < 189; i += 3) { // Cada turno
    if (num.nextDouble() < 0.5) {
        // Herda turno completo do pai 0
        filhos[0][i] = pais[0][i];
        filhos[0][i + 1] = pais[0][i + 1];
        filhos[0][i + 2] = pais[0][i + 2];
    } else {
        // Herda turno completo do pai 1
        filhos[0][i] = pais[1][i];
        filhos[0][i + 1] = pais[1][i + 1];
        filhos[0][i + 2] = pais[1][i + 2];
    }
}
```

**Benefício**: Garante que filhos herdam turnos válidos (3 médicos diferentes) diretamente dos pais.

> Se `nextDouble() >= pc`: filhos são cópias dos pais (sem cruzamento).

---

### ✅ Etapa 6: Mutação — `mutacao()`

Aplica **mutação com validação**: para cada turno, com probabilidade `pm`, substitui um gene por um médico sorteado, **garantindo que não se repete** no mesmo turno.

```java
for (int i = 0; i < 189; i += 3) { // cada turno
    if (pos.nextDouble() < pm) {
        int m1 = filhos[k][i];
        int m2 = filhos[k][i + 1];
        int m3 = filhos[k][i + 2];
        
        int vagaMutar = pos.nextInt(3);
        int medicoNovoAleatorio;
        
        do {
            medicoNovoAleatorio = pos.nextInt(25);
        } while (medicoNovoAleatorio == m1 || 
                 medicoNovoAleatorio == m2 || 
                 medicoNovoAleatorio == m3);
        
        filhos[k][i + vagaMutar] = medicoNovoAleatorio;
    }
}
```

**Benefício**: Mantém a restrição de médicos diferentes válida mesmo após mutação.

> Com `pm = 0.15` e 63 turnos por filho, em média ~9,45 turnos mutam por filho por geração — alta exploração.

---

### ✅ Etapa 7: Elitismo — `aplicarElitismo()`

Preserva os 2 melhores indivíduos da geração anterior **ANTES** de gerar novos filhos, garantindo que não sejam perdidos.

```java
// No início de cada geração (antes de gerar filhos)
aplicarElitismo();
popFilhos[0] = pop[0].clone();  // Melhor indivíduo
popFilhos[1] = pop[1].clone();  // 2º melhor indivíduo

// Filhos novos são inseridos em popFilhos[2] até popFilhos[tamPop-1]
int contFilhos = 2;
```

**Benefício**: Converge mais rápido mantendo a melhor solução de todas as gerações.

---

### ✅ Etapa 8: Loop Principal — `aG()`

Orquestra todas as etapas em loop até atingir o número máximo de gerações.

```
popInicial()
  └─ do {
       avaliacao()
       ordenacao()
       registro()         ← salva melhor se notas[0] < melhorNota
       aplicarElitismo()  ← preserva 2 melhores
       do {
           selecao()      ← com torneio(7 ou 10)
           cruzamento()   ← por turno
           mutacao()      ← com validação
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

## 🔧 Parâmetros Recomendados

| Parâmetro | Valor Recomendado | Descrição |
|-----------|------------------|-----------|
| `tamPop` | 50-500 | Tamanho da população — maior = mais exploração, mais lento |
| `maxGen` | 100-1000 | Número de gerações — mais = melhor convergência |
| `pc` | 0.80-0.90 | Probabilidade de cruzamento — geralmente alto |
| `pm` | **0.10-0.20** | **Probabilidade de mutação — CRÍTICO para exploração** |
| `torneio(k)` | 7-10 | Tamanho do torneio — maior = pressão seletiva |
| `Peso CG` | 5-10 | Clínica Geral é restrição crucial |

### ⚠️ Nota Importante Sobre `pm`

**`pm` muito baixa (0.01-0.05)** resulta em convergência prematura e qualidade de solução ruim.

**`pm` muito alta (0.30+)** causa muita perturbação — destrói boas soluções.

**Valor ótimo**: `pm ≈ 0.15` (aproximadamente **9-10 turnos** mutam por indivíduo por geração com 63 turnos totais).

---

## 📊 Restrições do Problema

### Restrições Duras (penalizadas com peso maior)

| Restrição | Peso | Descrição |
|-----------|------|-----------|
| Turno sem Clínica Geral | **5** | Cada turno deve ter ≥1 médico 0–4 |
| Médico em múltiplas unidades | **5** | Médico não pode estar em 2+ unidades no mesmo turno/dia |
| Turnos consecutivos | 3 | Médico não pode trabalhar turno seguido (Manhã→Tarde, Tarde→Noite, Noite→Manhã-dia-seguinte) |
| Excesso de carga horária | 2 | Médico limitado a máximo 5 turnos/semana (40h) |

**Nota**: Médicos diferentes por turno é garantido na população inicial — não precisa penalização.

---

## 📊 Função de Fitness

```
fitness = (sem_CG × 5) + (multiplas_unidades × 5) + (consecutivos × 3) + (excesso_horas × 2)
```

**Menores valores = melhores soluções.**

`fitness = 0` representaria a escala ideal — sem nenhuma violação.

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

## ✨ Resumo das Melhorias Implementadas

### v2.0 - Otimizações de Qualidade e Convergência

| Melhorias | Impacto | Status |
|-----------|---------|--------|
| **Cruzamento por turno** | Garante validade dos filhos | ✅ Implementada |
| **Mutação com validação** | Mantém restrição de médicos diferentes | ✅ Implementada |
| **Elitismo corrigido** | Preserva 2 melhores antes de gerar filhos | ✅ Implementada |
| **Torneio com k=7** | Pressão seletiva balanceada | ✅ Implementada |
| **pm ≈ 0.15** | Exploração adequada (9-10 mutações/indivíduo) | 🟡 Recomendado |
| **Penalização múltiplas unidades** | Detecta médico em 2+ unidades mesmo turno | ✅ Implementada |

### Diagnóstico de Desempenho

**Problema Original**: Convergência lenta (320 → 143 em 1000 gerações = 57% melhora)

**Causa Identificada**: `pm = 0.02` crítico para otimização — apenas 1-2 turnos mutam por indivíduo

**Solução**: Aumentar `pm` para 0.15 — aumenta 7.5x a exploração

---

## 🔍 Dicas de Debugging

### Se a melhor nota não diminui muito:
1. ✅ Verificar se `tamPop` e `maxGen` são suficientes
2. ✅ **Aumentar `pm` para 0.15** (impacto maior)
3. ✅ Aumentar `k` no torneio (7 → 10)
4. ✅ Verificar pesos das penalizações — Clínica Geral é crítica

### Se a nota piora entre gerações:
1. ✅ Confirmar que `aplicarElitismo()` é chamada **ANTES** de gerar filhos
2. ✅ Verificar que `contFilhos` começa em 2 (não 0)
3. ✅ Verificar que melhores indivíduos são clonados

### Se há muitos conflitos de Clínica Geral:
1. ✅ Aumentar peso de `penalizaClinicaGeral()` de 5 para 10
2. ✅ Verificar se há médicos suficientes na especialidade (5 disponíveis para 63 slots)
3. ✅ Considerar aumentar tamanho da população

### Se há repetição de médicos no turno:
1. ✅ Verificar `popInicial()` — deve ter validação do-while
2. ✅ Verificar `mutacao()` — deve validar que novo médico ≠ médicos existentes
3. ✅ Verificar `cruzamento()` — turno herda 3 médicos diferentes do pai