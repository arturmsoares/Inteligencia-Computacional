import java.util.Scanner;

public class Perceptron4 {

    private double x[][] = {

            { 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1 }, // 0
            { 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1 }, // 1
            { 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1 }, // 2
            { 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1 }, // 3
            { 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1 }, // 4
            { 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1 }, // 5
            { 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1 }, // 6
            { 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 }, // 7
            { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1 }, // 8
            { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1 } // 9

    };
    // padrões de entrada (dígitos 0–9 em matriz 4x4)

    private double w[][] = new double[16][4];
    // Matriz de pesos. 16 são os pesos para cada uma das entradas e 4 são os
    // perceptrons

    private double t[][] = {
            { 1, 1, 1, 1 }, // 0
            { 1, 1, 1, -1 }, // 1
            { 1, 1, -1, 1 }, // 2
            { 1, 1, -1, -1 }, // 3
            { 1, -1, 1, 1 }, // 4
            { 1, -1, 1, -1 }, // 5
            { 1, -1, -1, 1 }, // 6
            { 1, -1, -1, -1 }, // 7
            { -1, 1, 1, 1 }, // 8
            { -1, 1, 1, -1 } // 9
    };
    // Matriz de saídas desejadas

    private int epocas;

    public double[][] algoritmo(double alfa, double limiar) {

        double yent, f;
        boolean mudou;
        epocas = 0;

        do {

            mudou = false;

            for (int i = 0; i < 10; i++) {

                for (int k = 0; k < 4; k++) {

                    yent = somatorio(i, k);
                    f = saida(yent, limiar);

                    if (f != t[i][k]) {

                        atualiza(alfa, f, i, k);
                        mudou = true;

                    }
                }
            }

            epocas++;

        } while (mudou == true);

        return w;
    }

    public double somatorio(int padrao, int perceptron) {

        double soma = 0;

        for (int j = 0; j < 16; j++) {

            soma += w[j][perceptron] * x[padrao][j];

        }

        return soma;
    }

    public double saida(double yent, double limiar) {

        if (yent > limiar) {
            return 1;

        } else if (yent < -limiar) {
            return -1;

        } else {

            return 0;

        }
    }

    public void atualiza(double alfa, double f, int padrao, int perceptron) {

        for (int j = 0; j < 16; j++) {

            w[j][perceptron] += (alfa * (t[padrao][perceptron] - f) * x[padrao][j]);

        }
    }

    public void testarEntradas(double pesos[][], double limiar, Scanner s) {

        double entrada[] = new double[16];

        System.out.println("\nDigite 16 valores de entrada (0 ou 1):");

        for (int i = 0; i < 16; i++) {

            entrada[i] = s.nextDouble();

        }

        double saidas[] = new double[4];

        for (int k = 0; k < 4; k++) {

            double yent = 0;

            for (int j = 0; j < 16; j++) {

                yent += pesos[j][k] * entrada[j];

            }

            saidas[k] = saida(yent, limiar);

        }

        System.out.print("\nSaída dos perceptrons: ");

        for (int k = 0; k < 4; k++) {

            System.out.print((int) saidas[k] + " ");

        }

        System.out.println();
    }

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);

        double alfa, limiar;

        do {

            System.out.print("Digite o valor de alfa (entre 0 e 1): ");
            alfa = s.nextDouble();

            if (alfa <= 0 || alfa > 1) {

                System.out.println("Erro! Alfa deve estar entre 0 e 1.");

            }

        } while (alfa <= 0 || alfa > 1);

        System.out.print("Digite o valor do limiar: ");
        limiar = s.nextDouble();

        Perceptron4 p = new Perceptron4();

        double pesos[][] = p.algoritmo(alfa, limiar);

        System.out.println("\nTreinamento concluído em " + p.epocas + " épocas.");

        System.out.println("Pesos finais: ");

        for (int j = 0; j < 16; j++) {

            for (int k = 0; k < 4; k++) {

                System.out.println("w[" + j + "][" + k + "] = " + pesos[j][k]);

            }

        }

        p.testarEntradas(pesos, limiar, s);

        s.close();
    }
}