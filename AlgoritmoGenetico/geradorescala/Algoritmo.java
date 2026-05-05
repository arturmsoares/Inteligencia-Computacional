package geradorescala;

import java.util.Random;

public class Algoritmo {

    private int tamPop, maxGen;
    private double pc, pm;
    public int[][] pop;
    private int[][] popFilhos;
    public double[] notas;
    public int[] conflitos;

    public Algoritmo(int tamPop, int maxGen, double pc, double pm) {
        this.tamPop = tamPop;
        this.maxGen = maxGen;
        this.pc = pc;
        this.pm = pm;
        pop = new int[tamPop][189];
        popFilhos = new int[tamPop][189];
        notas = new double[tamPop];
        conflitos = new int[189];
    }

    // População inicial aleatória
    public void popInicial() {
        Random rand = new Random();

        for (int ind = 0; ind < tamPop; ind++) {
            int gene = 0;

            for (int u = 0; u < 3; u++) // 3 unidades
                for (int d = 0; d < 7; d++) // 7 dias
                    for (int t = 0; t < 3; t++) { // 3 turnos

                        pop[ind][gene++] = rand.nextInt(25);
                        pop[ind][gene++] = rand.nextInt(25);
                        pop[ind][gene++] = rand.nextInt(25);
                    }
        }
    }

    // Avaliação da população
    public void avaliacao() {
        for (int ind = 0; ind < tamPop; ind++) {
            notas[ind] = 0;
            notas[ind] += penalizaRepetidos(ind);
            notas[ind] += penalizaClinicaGeral(ind);
            notas[ind] += penalizaCargaHoraria(ind);
            notas[ind] += penalizaConsecutivos(ind);
        }
    }

    private int penalizaCargaHoraria(int ind) {
        int pen = 0;
        int peso = 2;
        int[] turnos = new int[25]; // contador de turnos por médico

        for (int gene = 0; gene < 189; gene++)
            turnos[pop[ind][gene]]++; // conta aparições de cada médico

        for (int med = 0; med < 25; med++)
            if (turnos[med] > 5)
                pen += (turnos[med] - 5); // penaliza cada turno excedente

        return pen * peso;
    }

    private int penalizaClinicaGeral(int ind) {
        int pen = 0;
        int peso = 1;
        for (int gene = 0; gene < 189; gene += 3) { // avança turno a turno
            int m1 = pop[ind][gene];
            int m2 = pop[ind][gene + 1];
            int m3 = pop[ind][gene + 2];

            // médicos 0–4 (Clinico Geral)
            boolean temCG = (m1 <= 4) || (m2 <= 4) || (m3 <= 4);
            if (!temCG)
                pen++;
        }
        return pen * peso;
    }

    private double penalizaRepetidos(int ind) {
        int pen = 0;
        int peso = 3;
        for (int gene = 0; gene < 189; gene += 3) { // cada turno ocupa 3 genes
            int medico1 = pop[ind][gene];
            int medico2 = pop[ind][gene + 1];
            int medico3 = pop[ind][gene + 2];

            if (medico1 == medico2 || medico1 == medico3 || medico2 == medico3) {
                pen++;
            }
        }
        return pen * peso;
    }

    private int penalizaConsecutivos(int ind) {
        int pen = 0;
        int peso = 2;

        for (int med = 0; med < 25; med++) { // para cada médico
            for (int d = 0; d < 7; d++) { // para cada dia
                for (int t = 0; t < 2; t++) { // turnos 0→1 e 1→2
                    boolean estaAtual = false;
                    boolean estaProximo = false;

                    for (int u = 0; u < 3; u++) { // verifica em todas unidades
                        int geneAtual = (u * 63) + (d * 9) + (t * 3);
                        int geneProx = (u * 63) + (d * 9) + ((t + 1) * 3);

                        for (int vagaTurno = 0; vagaTurno < 3; vagaTurno++) {
                            if (pop[ind][geneAtual + vagaTurno] == med)
                                estaAtual = true;
                            if (pop[ind][geneProx + vagaTurno] == med)
                                estaProximo = true;
                        }
                    }
                    if (estaAtual && estaProximo)
                        pen++;
                }

                // verifica Noite e Manhã do dia seguinte
                if (d < 6) {
                    boolean estaNoite = false;
                    boolean estaManha = false;
                    for (int u = 0; u < 3; u++) {
                        int geneNoite = (u * 63) + (d * 9) + (2 * 3);
                        int geneManha = (u * 63) + ((d + 1) * 9) + (0 * 3);
                        for (int p = 0; p < 3; p++) {
                            if (pop[ind][geneNoite + p] == med)
                                estaNoite = true;
                            if (pop[ind][geneManha + p] == med)
                                estaManha = true;
                        }
                    }
                    if (estaNoite && estaManha)
                        pen++;
                }
            }
        }
        return pen * peso;
    }

    public void ordenacao() {
        double auxNota;
        int[] auxInd;

        for (int i = 0; i < tamPop - 1; i++)
            for (int j = i + 1; j < tamPop; j++)
                if (notas[i] > notas[j]) { // ordena por nota (menor é melhor)
                    auxNota = notas[i];
                    notas[i] = notas[j];
                    notas[j] = auxNota;

                    auxInd = pop[i];
                    pop[i] = pop[j];
                    pop[j] = auxInd;
                }

    }

    public int[][] selecao(int[][] pais, int contGen) {
        pais[0] = torneio(3); //torneio entre 3 candidatos
        pais[1] = torneio(3);
        return pais;
    }

    private int[] torneio(int k) {
        Random num = new Random();
        int melhorPos = num.nextInt(tamPop);

        for (int i = 1; i < k; i++) {
            int desafiante = num.nextInt(tamPop);
            if (notas[desafiante] < notas[melhorPos])
                melhorPos = desafiante;
        }
        return pop[melhorPos].clone();
    }

    public int[][] cruzamento(int[][] pais, int[][] filhos) {
        Random num = new Random();

        if (num.nextDouble() < pc) {
            for (int j = 0; j < 189; j++) {
                // cada gene tem 50% de chance de vir de cada pai
                if (num.nextDouble() < 0.5) {
                    filhos[0][j] = pais[0][j];
                    filhos[1][j] = pais[1][j];
                } else {
                    filhos[0][j] = pais[1][j];
                    filhos[1][j] = pais[0][j];
                }
            }
        } else {
            filhos[0] = pais[0].clone();
            filhos[1] = pais[1].clone();
        }
        return filhos;
    }

    public int[][] mutacao(int[][] filhos) {
        Random pos = new Random();

        for (int k = 0; k < 2; k++) {
            for (int i = 0; i < 189; i += 3) { // para cada turno
                if (pos.nextDouble() < pm) {

                    // reinserção aleatória
                    int vagaAleatoria = i + pos.nextInt(3);
                    filhos[k][vagaAleatoria] = pos.nextInt(25);
                }
            }
        }
        return filhos;
    }

    public ResultadoAG aG() {
        int[][] pais = new int[2][189];
        int[][] filhos = new int[2][189];
        int[] melhorIndividuo = new int[189];
        double melhorNota = Double.MAX_VALUE;
        double notaInicial = 0;
        int melhorGeracao = 0;
        int contGen = 0;

        popInicial();
        avaliacao();
        ordenacao();
        notaInicial = notas[0];

        do {
            if (notas[0] < melhorNota) {
                melhorNota = notas[0];
                melhorGeracao = contGen;
                melhorIndividuo = registro(contGen, melhorIndividuo);
            }

            System.out.println("Geração: " + contGen +
                    " | Melhor nota: " + notas[0] +
                    " | Pior nota: " + notas[tamPop - 1]);

            int contFilhos = 0;
            do {
                pais = selecao(pais, contGen);
                filhos = cruzamento(pais, filhos);
                filhos = mutacao(filhos);
                insereFilhos(filhos, contFilhos);
                contFilhos += 2;
            } while (contFilhos < tamPop);

            for (int i = 0; i < tamPop; i++)
                pop[i] = popFilhos[i].clone(); // atualiza população com os filhos gerados

            contGen++;
            avaliacao(); // ← movido para antes do próximo if
            ordenacao();

        } while (contGen < maxGen);

        System.out.println("\n=== FIM ===");
        System.out.println("Nota inicial:  " + notaInicial);
        System.out.println("Melhor nota:   " + melhorNota + " | Geração: " + melhorGeracao);

        return new ResultadoAG(melhorIndividuo, melhorNota, notaInicial, melhorGeracao);
    }

    public void insereFilhos(int[][] filhos, int contFilhos) {
        for (int j = 0; j < 189; j++) {
            popFilhos[contFilhos][j] = filhos[0][j];
            popFilhos[contFilhos + 1][j] = filhos[1][j];
        }
    }

    public int[] registro(int contGen, int[] melhorIndividuo) {

        // salva o melhor indivíduo
        for (int j = 0; j < 189; j++) {
            melhorIndividuo[j] = pop[0][j];
            conflitos[j] = 0; // zera conflitos anteriores
        }

        // marca genes com médico repetido no turno
        for (int gene = 0; gene < 189; gene += 3) {
            int m1 = melhorIndividuo[gene];
            int m2 = melhorIndividuo[gene + 1];
            int m3 = melhorIndividuo[gene + 2];
            if (m1 == m2 || m1 == m3 || m2 == m3) {
                conflitos[gene] = 1;
                conflitos[gene + 1] = 1;
                conflitos[gene + 2] = 1;
            }
        }

        // marca genes sem Clínica Geral
        for (int gene = 0; gene < 189; gene += 3) {
            int m1 = melhorIndividuo[gene];
            int m2 = melhorIndividuo[gene + 1];
            int m3 = melhorIndividuo[gene + 2];
            if (m1 > 4 && m2 > 4 && m3 > 4) {
                conflitos[gene] = 1;
                conflitos[gene + 1] = 1;
                conflitos[gene + 2] = 1;
            }
        }
        return melhorIndividuo;
    }

}
