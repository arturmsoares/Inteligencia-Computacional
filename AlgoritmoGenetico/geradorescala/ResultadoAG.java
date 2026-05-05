package geradorescala;

public class ResultadoAG {
    public int[] melhorIndividuo;
    public double melhorNota;
    public double notaInicial;
    public int melhorGeracao;

    public ResultadoAG(int[] melhorIndividuo, double melhorNota, 
                       double notaInicial, int melhorGeracao) {
        this.melhorIndividuo = melhorIndividuo;
        this.melhorNota      = melhorNota;
        this.notaInicial     = notaInicial;
        this.melhorGeracao   = melhorGeracao;
    }
}