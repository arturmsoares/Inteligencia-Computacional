
import java.util.Scanner;

public class Perceptron4 {

    // Matriz de entradas (10 padrões de 16 posições)
    double[][] x = new double[10][16];

    // Matriz de saídas desejadas
    int[][] t = {
            { -1, -1, -1, -1 }, // 0
            { -1, -1, -1, 1 }, // 1
            { -1, -1, 1, -1 }, // 2
            { -1, -1, 1, 1 }, // 3
            { -1, 1, -1, -1 }, // 4
            { -1, 1, -1, 1 }, // 5
            { -1, 1, 1, -1 }, // 6
            { -1, 1, 1, 1 }, // 7
            { 1, -1, -1, -1 }, // 8
            { 1, -1, -1, 1 } // 9
    };

    double[][] w = new double[16][4]; // pesos
    double limiar = 0.0;
    double alfa = 0.5;

    // Construtor que inicializa os padrões
    public Perceptron4() {

        x[0] = new double[] {
                1, 1, 1, 1,
                1, 0, 0, 1,
                1, 0, 0, 1,
                1, 1, 1, 1
        };

        x[1] = new double[] {
                0, 0, 1, 0,
                0, 1, 1, 0,
                1, 0, 1, 0,
                0, 0, 1, 0
        };

        x[2] = new double[] {
                1, 1, 1, 1,
                0, 0, 0, 1,
                1, 1, 1, 1,
                1, 0, 0, 0
        };

        x[3] = new double[] {
                1, 1, 1, 1,
                0, 0, 0, 1,
                0, 1, 1, 1,
                1, 1, 1, 1
        };

        x[4] = new double[] {
                1, 0, 0, 1,
                1, 0, 0, 1,
                1, 1, 1, 1,
                0, 0, 0, 1
        };

        x[5] = new double[] {
                1, 1, 1, 1,
                1, 0, 0, 0,
                1, 1, 1, 1,
                0, 0, 0, 1
        };

        x[6] = new double[] {
                1, 1, 1, 1,
                1, 0, 0, 0,
                1, 1, 1, 1,
                1, 0, 0, 1
        };

        x[7] = new double[] {
                1, 1, 1, 1,
                0, 0, 0, 1,
                0, 0, 1, 0,
                0, 1, 0, 0
        };

        x[8] = new double[] {
                1, 1, 1, 1,
                1, 0, 0, 1,
                1, 1, 1, 1,
                1, 0, 0, 1
        };

        x[9] = new double[] {
                1, 1, 1, 1,
                1, 0, 0, 1,
                1, 1, 1, 1,
                0, 0, 0, 1
        };
    }

    // Função de ativação
    public int funcaoAtivacao(double yent) {
        if (yent > limiar)
            return 1;
        else if (yent < -limiar)
            return -1;
        else
            return 0;
    }

    // Treinamento
    public void treinar() {

        boolean mudou;
        int epocas = 0;

        do {

            mudou = false;

            for (int i = 0; i < 10; i++) {
                for (int k = 0; k < 4; k++) {
                    double yent = 0;
                    for (int j = 0; j < 16; j++) {
                        yent += x[i][j] * w[j][k];
                    }
                    int f = funcaoAtivacao(yent);
                    if (f != t[i][k]) {
                        for (int j = 0; j < 16; j++) {
                            w[j][k] += alfa * (t[i][k] - f) * x[i][j];
                        }
                        mudou = true;
                    }
                }
            }
            epocas++;
        } while (mudou);
        System.out.println("Treinamento finalizado em " + epocas + " épocas.");
    }

    // Teste da rede
    public void testar() {

        Scanner sc = new Scanner(System.in);
        double[] entrada = new double[16];

        System.out.println("Digite os 16 valores da entrada:");

        for (int i = 0; i < 16; i++) {
            entrada[i] = sc.nextDouble();
        }

        int[] saida = new int[4];

        for (int k = 0; k < 4; k++) {

            double yent = 0;

            for (int j = 0; j < 16; j++) {
                yent += entrada[j] * w[j][k];
            }

            saida[k] = funcaoAtivacao(yent);
        }

        System.out.print("Saída da rede: ");

        for (int i = 0; i < 4; i++) {
            System.out.print(saida[i] + " ");
        }

        System.out.println();
    }

    // Programa principal
    public static void main(String[] args) {

        Perceptron4 p = new Perceptron4();

        p.treinar();
        p.testar();
    }
}