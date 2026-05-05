package geradorescala;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class Interface extends JFrame {

    // ── Componentes ──────────────────────────────────────────────
    private JTextField tfTamPop, tfMaxGen, tfPc, tfPm;
    private JTextField tfMelhorNota, tfMelhorGeracao;
    private JButton btnExecutar, btnLimpar;
    private JTextPane areaLog;
    private JTable tabelaEscala;
    private DefaultTableModel modeloTabela;
    private JLabel lblStatus;

    // ── Estilos de texto ─────────────────────────────────────────
    private SimpleAttributeSet estiloNormal;
    private SimpleAttributeSet estiloDestaque;
    private SimpleAttributeSet estiloErro;
    private SimpleAttributeSet estiloCabecalho;

    // ── Nomes dos médicos e especialidades ───────────────────────
    private static final String[] ESPECIALIDADES = {
            "Clínica Geral", "Pediatria", "Ginecologia", "Ortopedia", "Cardiologia"
    };
    private static final String[] DIAS = {
            "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"
    };
    private static final String[] TURNOS = { "Manhã", "Tarde", "Noite" };

    // ─────────────────────────────────────────────────────────────
    public Interface() {
        super("🧬 Gerador de Escala Médica — Algoritmo Genético");
        initEstilos();
        initComponents();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    // ── Estilos do log ────────────────────────────────────────────
    private void initEstilos() {
        estiloNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(estiloNormal, "Monospaced");
        StyleConstants.setFontSize(estiloNormal, 12);
        StyleConstants.setForeground(estiloNormal, Color.BLACK);

        estiloDestaque = new SimpleAttributeSet();
        StyleConstants.setBold(estiloDestaque, true);
        StyleConstants.setFontSize(estiloDestaque, 13);
        StyleConstants.setForeground(estiloDestaque, new Color(0, 100, 0));

        estiloErro = new SimpleAttributeSet();
        StyleConstants.setBold(estiloErro, true);
        StyleConstants.setFontSize(estiloErro, 12);
        StyleConstants.setForeground(estiloErro, Color.RED);

        estiloCabecalho = new SimpleAttributeSet();
        StyleConstants.setBold(estiloCabecalho, true);
        StyleConstants.setFontSize(estiloCabecalho, 14);
        StyleConstants.setForeground(estiloCabecalho, Color.WHITE);
        StyleConstants.setBackground(estiloCabecalho, new Color(50, 50, 50));
    }

    // ── Monta a janela ────────────────────────────────────────────
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(1100, 700));

        add(painelParametros(), BorderLayout.NORTH);
        add(painelCentral(), BorderLayout.CENTER);
        add(painelStatus(), BorderLayout.SOUTH);

        pack();
    }

    // ── Painel de parâmetros (topo) ───────────────────────────────
    private JPanel painelParametros() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        p.setBorder(BorderFactory.createTitledBorder("⚙️  Parâmetros do AG"));
        p.setBackground(new Color(240, 240, 245));

        tfTamPop = campo("100", 5);
        tfMaxGen = campo("500", 5);
        tfPc = campo("0.85", 5);
        tfPm = campo("0.02", 5);

        p.add(rotulo("Tam. Pop:"));
        p.add(tfTamPop);
        p.add(rotulo("Gerações:"));
        p.add(tfMaxGen);
        p.add(rotulo("P. Cruzamento:"));
        p.add(tfPc);
        p.add(rotulo("P. Mutação:"));
        p.add(tfPm);
        p.add(Box.createHorizontalStrut(20));

        btnExecutar = new JButton("▶ Executar AG");
        btnExecutar.setBackground(new Color(34, 139, 34));
        btnExecutar.setForeground(Color.WHITE);
        btnExecutar.setFont(new Font("Arial", Font.BOLD, 13));
        btnExecutar.addActionListener(e -> executarAG());

        btnLimpar = new JButton("🗑 Limpar");
        btnLimpar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnLimpar.addActionListener(e -> limpar());

        p.add(btnExecutar);
        p.add(btnLimpar);

        // campos de resultado
        p.add(Box.createHorizontalStrut(30));
        p.add(rotulo("Melhor Nota:"));
        tfMelhorNota = campo("—", 6);
        tfMelhorNota.setEditable(false);
        tfMelhorNota.setBackground(new Color(220, 255, 220));
        p.add(tfMelhorNota);

        p.add(rotulo("Geração:"));
        tfMelhorGeracao = campo("—", 5);
        tfMelhorGeracao.setEditable(false);
        tfMelhorGeracao.setBackground(new Color(220, 255, 220));
        p.add(tfMelhorGeracao);

        return p;
    }

    // ── Painel central: log + tabela ──────────────────────────────
    private JSplitPane painelCentral() {
        // LOG
        areaLog = new JTextPane();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("📋 Log de Execução"));
        scrollLog.setPreferredSize(new Dimension(400, 500));

        // TABELA DA ESCALA
        modeloTabela = new DefaultTableModel();
        tabelaEscala = new JTable(modeloTabela) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelaEscala.setRowHeight(22);
        tabelaEscala.setFont(new Font("Monospaced", Font.PLAIN, 11));
        tabelaEscala.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        tabelaEscala.getTableHeader().setBackground(new Color(70, 100, 140));
        tabelaEscala.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollTabela = new JScrollPane(tabelaEscala);
        scrollTabela.setBorder(BorderFactory.createTitledBorder("📅 Melhor Escala Encontrada"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLog, scrollTabela);
        split.setDividerLocation(400);
        split.setResizeWeight(0.35);
        return split;
    }

    // ── Barra de status (rodapé) ──────────────────────────────────
    private JPanel painelStatus() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(new Color(220, 220, 230));
        lblStatus = new JLabel("Pronto. Configure os parâmetros e clique em Executar AG.");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        p.add(lblStatus);
        return p;
    }

    // ── Ação: executar AG ─────────────────────────────────────────
    private void executarAG() {
        try {
            int tamPop = Integer.parseInt(tfTamPop.getText().trim());
            int maxGen = Integer.parseInt(tfMaxGen.getText().trim());
            double pc = Double.parseDouble(tfPc.getText().trim());
            double pm = Double.parseDouble(tfPm.getText().trim());

            limpar();
            btnExecutar.setEnabled(false);
            lblStatus.setText("⏳ Executando AG...");

            // roda em thread separada para não travar a interface
            SwingWorker<int[], Void> worker = new SwingWorker<>() {
                @Override
                protected int[] doInBackground() {
                    Algoritmo alg = new Algoritmo(tamPop, maxGen, pc, pm);

                    log("═══════════════════════════════════════\n", estiloCabecalho);
                    log(" 🧬 INICIANDO ALGORITMO GENÉTICO\n", estiloCabecalho);
                    log("═══════════════════════════════════════\n", estiloCabecalho);
                    log("Tam. Pop: " + tamPop + " | Gerações: " + maxGen +
                            " | pc: " + pc + " | pm: " + pm + "\n\n", estiloNormal);

                    log("▶ Executando AG...\n", estiloNormal);
                    ResultadoAG resultado = alg.aG();

                    SwingUtilities.invokeLater(() -> {
                        log("✔ Nota inicial:      " + resultado.notaInicial + "\n", estiloNormal);
                        log("✔ Melhor nota final: " + resultado.melhorNota + "\n", estiloDestaque);
                        log("✔ Melhor geração:    " + resultado.melhorGeracao + "\n", estiloDestaque);
                        tfMelhorNota.setText(String.valueOf(resultado.melhorNota));
                        tfMelhorGeracao.setText(String.valueOf(resultado.melhorGeracao));
                        montarTabela(resultado.melhorIndividuo, alg.conflitos);
                    });

                    return resultado.melhorIndividuo;
                }

                @Override
                protected void done() {
                    btnExecutar.setEnabled(true);
                    lblStatus.setText("✅ AG finalizado! Veja o resultado na tabela à direita.");
                    log("\n✅ Execução concluída!\n", estiloDestaque);
                }
            };

            worker.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Verifique os parâmetros — todos devem ser números válidos.",
                    "Erro de entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Monta a tabela com a melhor escala ────────────────────────
    private void montarTabela(int[] melhor, int[] conflitos) {
        modeloTabela.setRowCount(0);
        modeloTabela.setColumnCount(0);

        modeloTabela.addColumn("Unidade");
        modeloTabela.addColumn("Dia");
        modeloTabela.addColumn("Turno");
        modeloTabela.addColumn("Médico 1");
        modeloTabela.addColumn("Médico 2");
        modeloTabela.addColumn("Médico 3");

        for (int u = 0; u < 3; u++)
            for (int d = 0; d < 7; d++)
                for (int t = 0; t < 3; t++) {
                    int gene = (u * 63) + (d * 9) + (t * 3);
                    modeloTabela.addRow(new Object[] {
                            "Unidade " + (u + 1),
                            DIAS[d],
                            TURNOS[t],
                            nomeMedico(melhor[gene]),
                            nomeMedico(melhor[gene + 1]),
                            nomeMedico(melhor[gene + 2])
                    });
                }

        // coloração das células com conflito
        tabelaEscala.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);

                if (col >= 3) {
                    // colunas 3,4,5 → médicos do turno
                    int gene = (row * 3) + (col - 3);
                    if (conflitos[gene] == 1)
                        c.setBackground(new Color(255, 180, 180)); // vermelho claro
                    else
                        c.setBackground(isSelected
                                ? table.getSelectionBackground()
                                : Color.WHITE);
                } else {
                    // colunas de contexto (Unidade, Dia, Turno)
                    c.setBackground(isSelected
                            ? table.getSelectionBackground()
                            : new Color(240, 240, 250));
                }
                return c;
            }
        });
    }

    // ── Converte índice de médico em nome legível ─────────────────
    private String nomeMedico(int idx) {
        String esp = ESPECIALIDADES[idx / 5];
        int num = (idx % 5) + 1;
        return "M" + String.format("%02d", idx + 1) + " (" + esp.substring(0, 3) + "." + num + ")";
    }

    // ── Utilitário: escreve no log ────────────────────────────────
    private void log(String texto, SimpleAttributeSet estilo) {
        SwingUtilities.invokeLater(() -> {
            Document doc = areaLog.getDocument();
            try {
                doc.insertString(doc.getLength(), texto, estilo);
                areaLog.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    // ── Limpa log e tabela ────────────────────────────────────────
    private void limpar() {
        areaLog.setText("");
        modeloTabela.setRowCount(0);
        modeloTabela.setColumnCount(0);
        tfMelhorNota.setText("—");
        tfMelhorGeracao.setText("—");
        lblStatus.setText("Pronto.");
    }

    // ── Utilitários de layout ──────────────────────────────────────
    private JTextField campo(String valor, int colunas) {
        JTextField tf = new JTextField(valor, colunas);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return tf;
    }

    private JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        return l;
    }

    // ── Main ──────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Interface::new);
    }
}