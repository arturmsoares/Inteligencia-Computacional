import java.util.Scanner;

public class Perceptron {

    private double x[][] = { { 1, 1, 1, 1 },
            { 1, 1, 1, 0 },
            { 1, 1, 0, 1 },
            { 1, 1, 0, 0 },
            { 1, 0, 1, 1 },
            { 1, 0, 1, 0 },
            { 1, 0, 0, 1 },
            { 1, 0, 0, 0 } };
    private double w[] = { 0, 0, 0, 0 }; //iniciando com pesos iguais a zero
    private double t[] = { 1, -1, -1, -1, -1, -1, -1, -1 }; // AND
    private int epocas;

    public double[] algoritmo(double alfa, double limiar) {
        double yent, f;
        boolean mudou;
        epocas = 0;

        do {
            mudou = false;
            for (int i = 0; i < x.length; i++) {
                yent = somatorio(i);
                f = saida(yent, limiar);
                if (f != t[i]) {
                    atualiza(alfa, f, i);
                    mudou = true;
                }
            }
            epocas++;
        } while (mudou == true);
        return w;
    }

    public double somatorio(int i) {
        double soma = 0;
        for (int j = 0; j < w.length; j++) {
            soma += w[j] * x[i][j];
        }
        return soma;
    }

    public double saida(double yent, double limiar) {
        if (yent > limiar) {
            return 1;
        } else if(yent < -limiar) {
            return -1;
        } else {
            return 0;
        }
    }

    public void atualiza(double alfa, double f, int i) {
        for (int j = 0; j < w.length; j++) {
            w[j] = w[j] + (alfa * (t[i] - f) * x[i][j]);
        }
    }

    public void testarEntradas(double[] pesos, double limiar, Scanner s) {
        System.out.print("\nDigite 3 valores de entrada (x1, x2, x3) para teste: ");
        int entrada1 = s.nextInt();
        int entrada2 = s.nextInt();
        int entrada3 = s.nextInt();
        
        double yent = pesos[0] * 1 + pesos[1] * entrada1 + pesos[2] * entrada2 + pesos[3] * entrada3;
        double saidaTeste = saida(yent, limiar);

        System.out.println("\nValor de yent: " + yent);
        System.out.println("Saída do teste: " + (int)saidaTeste);
    }

    public static void main (String[] args) {
        Scanner s = new Scanner(System.in);
        double alfa, limiar;

        // indicação do valor de alfa
        do {
            System.out.print("Digite o valor de alfa (entre 0 e 1): ");
            alfa = s.nextDouble();
            if (alfa <= 0 || alfa > 1) {
                System.out.println("Erro! Alfa deve estar entre 0 e 1.");
            }
        } while (alfa <= 0 || alfa > 1);

        // indicação do valor do limiar
        System.out.print("Digite o valor do limiar: ");
        limiar = s.nextDouble();

        Perceptron p = new Perceptron();
        double pesos[] = p.algoritmo(alfa, limiar);

        System.out.println("\nTreinamento concluído em " + p.epocas + " épocas.");
        System.out.println("Pesos finais: ");
        for (int i = 0; i < pesos.length; i++) {
            System.out.println("w[" + i + "] = " + pesos[i]);
        }

        p.testarEntradas(pesos, limiar, s);

        s.close();   
    }

}


//fazer o teste de entradas  x saídas
