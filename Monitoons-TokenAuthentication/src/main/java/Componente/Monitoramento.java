package Componente;

import Autenticação.Usuario;
import Conexao.Conexao;
import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.discos.Disco;
import com.github.britooo.looca.api.group.discos.DiscoGrupo;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.processador.Processador;
import org.springframework.jdbc.core.JdbcTemplate;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Monitoramento {

    private Integer contadorVerificacoes = 0;
    private String processadorNome;
    private List<Disco> discos;
    private List<GraphicsCard> gpus;
    private Memoria memoria;
    private String memoriaNome;
    private Processador processador;


    public void comecarMonitoramento(Usuario usuario) throws InterruptedException, IOException {

        // Inicialização dos objetos necessários
        Integer idComputador = usuario.getIdComputadorMySQL();
        Looca looca = new Looca();
        Conexao conexao = new Conexao();
        JdbcTemplate jdbcTemplate = conexao.getConexaoDoBanco();
        InetAddress inetAddress = null;
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();

        // Iterar sobre as placas de vídeo e verificar se estão cadastradas no banco de dados
        try {
            Process process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu,memory.used,memory.free --format=csv,noheader,nounits");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for (GraphicsCard gpu : gpus) {
                String gpuNome = gpu.getName();
                String gpuFabricante = gpu.getVendor();
                String gpuVersao = gpu.getVersionInfo();
                Long gpuVRAM = gpu.getVRam();
                Long gpuMemoria = null;
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] memoryInfo = line.trim().split(",");
                    Double gpuMemoriaUso = Double.parseDouble(memoryInfo[1].trim());
                    Double gpuMemoriaDisponivel = Double.parseDouble(memoryInfo[2].trim());
                    gpuMemoria = (long) (gpuMemoriaDisponivel + gpuMemoriaUso);
                }

                // Verificar se a placa de vídeo está cadastrada no banco de dados
                Boolean existePlacaDeVideo = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM componente WHERE tipo = 'GPU' AND nome = ?", Integer.class, gpuNome) > 0;

                if (!existePlacaDeVideo) {
                    // Cadastrar placa de vídeo no banco de dados
                    jdbcTemplate.update("INSERT INTO componente (tipo, nome) VALUES (?, ?)", "GPU", gpuNome);
                    Integer idComponente = jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, gpuNome);

                    // Relacionar placa de vídeo ao computador
                    jdbcTemplate.update("INSERT INTO computadorhascomponente (fkComputador, fkComponente) VALUES (?, ?)", idComputador, idComponente);
                    componentesCadastrados.add(new Componente(idComponente, "GPU", gpuNome, List.of(
                            new Especificacao(idComponente, "Fabricante", gpuFabricante),
                            new Especificacao(idComponente, "Versão", gpuVersao),
                            new Especificacao(idComponente, "VRAM", Utilitarios.formatBytes(gpuVRAM)),
                            new Especificacao(idComponente, "Memória", Utilitarios.formatBytes(gpuMemoria))
                    )));

                    // Inserir especificações da placa de vídeo
                    for (Componente componenteCadastrado : componentesCadastrados) {
                        for (Especificacao especificacao : componenteCadastrado.getEspecificacoes()) {
                            if (especificacao.getFkComponente().equals(idComponente)) {
                                jdbcTemplate.update("INSERT INTO especificacoesComponente (fkComponente, tipoEspecificacao, valor) VALUES (?, ?, ?)", idComponente, especificacao.getTipo(), especificacao.getValor());
                            }
                        }
                    }
                } else {
                    // Verificar se a relação entre computador e placa de vídeo existe
                    Boolean compHasCompExiste = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM computadorhascomponente WHERE fkComputador = ? AND fkComponente = ?", Integer.class, idComputador, jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, gpuNome)) > 0;
                    if (!compHasCompExiste) {
                        // Se não existir, criar a relação
                        Integer idComponente = jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, gpuNome);
                        jdbcTemplate.update("INSERT INTO computadorhascomponente (fkComputador, fkComponente) VALUES (?, ?)", idComputador, idComponente);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Não tem nvidia-smi");
        }

        // Lista para armazenar os registros a serem inseridos no banco de dados
        List<Registro> registros = new ArrayList<>();
        List<Alerta> alertas = new ArrayList<>();

        // Iterar sobre os discos para obter informações de leitura e escrita
        for (Disco disco : discos) {
            Long velocidadeDeLeitura = disco.getBytesDeLeitura();
            Long velocidadeDeEscrita = disco.getBytesDeEscritas();

            // Obter IDs relacionados ao disco no banco de dados
            Integer idComponente = jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, disco.getModelo());
            Integer idCompHasComp = jdbcTemplate.queryForObject("SELECT idCompHasComp FROM computadorhascomponente WHERE fkComputador = ? AND fkComponente = ? ", Integer.class, idComputador, idComponente);

            // Adicionar registros de velocidade de leitura e escrita à lista
            registros.add(new Registro(idCompHasComp, "Velocidade de Leitura", Utilitarios.formatBytesToDouble(velocidadeDeLeitura / (1024 * 1204)), Utilitarios.formatBytesPerSecond(velocidadeDeLeitura / (1024 * 1204)), Utilitarios.getUnidadeBytesPerSecond(velocidadeDeLeitura / (1024 * 1204))));
            registros.add(new Registro(idCompHasComp, "Velocidade de Escrita", Utilitarios.formatBytesToDouble(velocidadeDeEscrita / (1024 * 1204)), Utilitarios.formatBytesPerSecond(velocidadeDeEscrita / (1024 * 1204)), Utilitarios.getUnidadeBytesPerSecond(velocidadeDeEscrita / (1024 * 1204))));

            // Obter índices dos registros de velocidade de leitura e escrita
//                Integer indexVelLeitura = 0;
//                Integer indexVelEscrita = 0;
//
//                for (components.Registro registro : registros) {
//                    if (registro.getTipo().equals("Velocidade de Leitura")) {
//                        indexVelLeitura = registros.indexOf(registro);
//                    }
//                    if (registro.getTipo().equals("Velocidade de Escrita")) {
//                        indexVelEscrita = registros.indexOf(registro);
//                    }
//                }
//
//                // Adicionar alertas de velocidade de leitura e escrita à lista
//                if ( velocidadeDeLeitura != null) {
//                    if (velocidadeDeLeitura > 1000000) {
//                        registros.get(indexVelLeitura).addAlerta(new components.Alerta("Alto", "Disco"));
//                    } else if (velocidadeDeLeitura > 100000) {
//                        registros.get(indexVelLeitura).addAlerta(new components.Alerta("Intemediário", "Disco"));
//                    } else if (velocidadeDeLeitura > 10000) {
//                        registros.get(indexVelLeitura).addAlerta(new components.Alerta("Médio", "Disco"));
//                    } else if (velocidadeDeLeitura > 1000) {
//                        registros.get(indexVelLeitura).addAlerta(new components.Alerta("Baixo", "Disco"));
//                    }
//                }
//
//                if (velocidadeDeEscrita != null) {
//                    if (velocidadeDeEscrita > 1000000) {
//                        registros.get(indexVelEscrita).addAlerta(new components.Alerta("Alto", "Disco"));
//                    } else if (velocidadeDeEscrita > 100000) {
//                        registros.get(indexVelEscrita).addAlerta(new components.Alerta("Intemediário", "Disco"));
//                    } else if (velocidadeDeEscrita > 10000) {
//                        registros.get(indexVelEscrita).addAlerta(new components.Alerta("Médio", "Disco"));
//                    } else if (velocidadeDeEscrita > 1000) {
//                        registros.get(indexVelEscrita).addAlerta(new components.Alerta("Baixo", "Disco"));
//                    }
//                }
        }

        //Iterar sobre as placas de vídeo para obter informações de uso da GPU
        try {
            Process process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu,memory.used,memory.free --format=csv,noheader,nounits");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            for (GraphicsCard gpu : gpus) {

                String gpuNome = gpu.getName();
                String line;
                Long gpuTotal = null;
                Long gpuMemUso = null;
                Long gpuMemDisp = null;
                Double videoPorcetUso = null;
                while ((line = reader.readLine()) != null) {
                    String[] memoryInfo = line.trim().split(",");
                    videoPorcetUso = Double.parseDouble(memoryInfo[0].trim());
                    gpuMemUso = Long.parseLong(memoryInfo[1].trim());
                    gpuMemDisp = Long.parseLong(memoryInfo[2].trim());
                    gpuTotal = (long) (gpuMemDisp + gpuMemUso);
                    System.out.println("Uso da Gpu: " + videoPorcetUso + "%");
                    System.out.println("Memória de Vídeo em Uso: " + Utilitarios.formatBytes(gpuMemUso));
                    System.out.println("Memória de Vídeo Disponível: " + Utilitarios.formatBytes(gpuMemDisp));
                }

                // Obter IDs relacionados à placa de vídeo no banco de dados
                Integer idComponente = jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, gpuNome);
                Integer idCompHasComp = jdbcTemplate.queryForObject("SELECT idCompHasComp FROM computadorhascomponente WHERE fkComputador = ? AND fkComponente = ? ", Integer.class, idComputador, idComponente);

                // Adicionar registro de uso da GPU à lista

                if (videoPorcetUso != null && gpuMemUso != null && gpuMemDisp != null) {
                    registros.add(new Registro(idCompHasComp, "Uso da GPU", Utilitarios.formatPercentagetoDouble(videoPorcetUso), videoPorcetUso.toString(), "%"));
                    registros.add(new Registro(idCompHasComp, "Memória de Vídeo em Uso", Utilitarios.formatBytesToDouble(gpuMemUso), Utilitarios.formatBytes(gpuMemUso), Utilitarios.getUnidadeBytes(gpuMemUso)));
                    registros.add(new Registro(idCompHasComp, "Memória de Vídeo Disponível", Utilitarios.formatBytesToDouble(gpuMemDisp), Utilitarios.formatBytes(gpuMemDisp), Utilitarios.getUnidadeBytes(gpuMemDisp)));
                }
                // Obter índices dos registros de uso da GPU e de memória de vídeo disponível
                Integer indexVideoUso = 0;
                Integer indexVideoMemDisp = 0;

                for (Registro registro : registros) {
                    if (registro.getTipo().equals("Uso da GPU")) {
                        indexVideoUso = registros.indexOf(registro);
                    }
                    if (registro.getTipo().equals("Memória de Vídeo Disponível")) {
                        indexVideoMemDisp = registros.indexOf(registro);
                    }
                }

                // Adicionar alertas de uso da GPU à lista
                if (videoPorcetUso != null) {
                    if (videoPorcetUso > 90) {
                        alertas.add(new Alerta(idCompHasComp, registros.get(indexVideoUso).getTipo(), registros.get(indexVideoUso).getValor(), registros.get(indexVideoUso).getValorFormatado(), registros.get(indexVideoUso).getUnidade(), indexVideoUso, "CRITICO", "GPU"));
                    } else if (videoPorcetUso > 80) {
                        alertas.add(new Alerta(idCompHasComp, registros.get(indexVideoUso).getTipo(), registros.get(indexVideoUso).getValor(), registros.get(indexVideoUso).getValorFormatado(), registros.get(indexVideoUso).getUnidade(), indexVideoUso, "INTERMEDIARIO", "GPU"));
                    } else if (videoPorcetUso > 70) {
                        alertas.add(new Alerta(idCompHasComp, registros.get(indexVideoUso).getTipo(), registros.get(indexVideoUso).getValor(), registros.get(indexVideoUso).getValorFormatado(), registros.get(indexVideoUso).getUnidade(), indexVideoUso, "MODERADO", "GPU"));
                    }
                }

                if (gpuMemDisp != null) {
                    if (gpuMemDisp < 1000) {
                        alertas.add(new Alerta(idCompHasComp, registros.get(indexVideoMemDisp).getTipo(), registros.get(indexVideoMemDisp).getValor(), registros.get(indexVideoMemDisp).getValorFormatado(), registros.get(indexVideoMemDisp).getUnidade(), indexVideoMemDisp, "CRITICO", "GPU"));
                    } else if (gpuMemDisp < 2000) {
                        alertas.add(new Alerta(idCompHasComp, registros.get(indexVideoMemDisp).getTipo(), registros.get(indexVideoMemDisp).getValor(), registros.get(indexVideoMemDisp).getValorFormatado(), registros.get(indexVideoMemDisp).getUnidade(), indexVideoMemDisp, "INTERMEDIARIO", "GPU"));
                    } else if (gpuMemDisp < 3000) {
                        alertas.add(new Alerta(idCompHasComp, registros.get(indexVideoMemDisp).getTipo(), registros.get(indexVideoMemDisp).getValor(), registros.get(indexVideoMemDisp).getValorFormatado(), registros.get(indexVideoMemDisp).getUnidade(), indexVideoMemDisp, "MODERADO", "GPU"));
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("Não tem nvidia-smi");
        }
        // Obter IDs relacionados à memória no banco de dados
        Integer idComponenteMemoria = jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, memoriaNome);
        Integer idCompHasCompMemoria = jdbcTemplate.queryForObject("SELECT idCompHasComp FROM computadorhascomponente WHERE fkComputador = ? AND fkComponente = ? ", Integer.class, idComputador, idComponenteMemoria);

        // Calcular e adicionar registros de memória disponível e em uso à lista
        Long memoriaDisponivel = memoria.getDisponivel();
        Long memoriaEmUso = memoria.getEmUso();
        registros.add(new Registro(idCompHasCompMemoria, "Memória Disponível", Utilitarios.formatBytesToDouble(memoriaDisponivel), Utilitarios.formatBytes(memoriaDisponivel), Utilitarios.getUnidadeBytes(memoriaDisponivel)));
        registros.add(new Registro(idCompHasCompMemoria, "Memória em Uso", Utilitarios.formatBytesToDouble(memoriaEmUso), Utilitarios.formatBytes(memoriaEmUso), Utilitarios.getUnidadeBytes(memoriaEmUso)));

        // Obter índice do registro de memória disponível
        Integer indexMemDisp = 0;

        for (Registro registro : registros) {
            if (registro.getTipo().equals("Memória Disponível")) {
                indexMemDisp = registros.indexOf(registro);
            }
        }
        // Adicionar alertas de memória disponível e em uso à lista
        if (memoriaDisponivel < 1) {
            alertas.add(new Alerta(idCompHasCompMemoria, registros.get(indexMemDisp).getTipo(), registros.get(indexMemDisp).getValor(), registros.get(indexMemDisp).getValorFormatado(), registros.get(indexMemDisp).getUnidade(), indexMemDisp, "CRITICO", "RAM"));
        } else if (memoriaDisponivel < 2) {
            alertas.add(new Alerta(idCompHasCompMemoria, registros.get(indexMemDisp).getTipo(), registros.get(indexMemDisp).getValor(), registros.get(indexMemDisp).getValorFormatado(), registros.get(indexMemDisp).getUnidade(), indexMemDisp, "INTERMEDIARIO", "RAM"));
        } else if (memoriaDisponivel < 3) {
            alertas.add(new Alerta(idCompHasCompMemoria, registros.get(indexMemDisp).getTipo(), registros.get(indexMemDisp).getValor(), registros.get(indexMemDisp).getValorFormatado(), registros.get(indexMemDisp).getUnidade(), indexMemDisp, "MODERADO", "RAM"));
        }

        // Obter IDs relacionados ao processador no banco de dados
        Integer idComponenteProcessador = jdbcTemplate.queryForObject("SELECT idComponente FROM componente WHERE nome = ?", Integer.class, processadorNome);
        Integer idCompHasCompProcessador = jdbcTemplate.queryForObject("SELECT idCompHasComp FROM computadorhascomponente WHERE fkComputador = ? AND fkComponente = ? ", Integer.class, idComputador, idComponenteProcessador);

        // Obter e adicionar registros de uso da CPU e temperatura à lista
        Double usoCpu = processador.getUso();
        registros.add(new Registro(idCompHasCompProcessador, "Uso da CPU", Utilitarios.formatPercentagetoDouble(usoCpu), Utilitarios.formatPercentage(usoCpu), "%"));

        // Obter índice do registro de uso da CPU
        Integer indexUsoCpu = 0;

        for (Registro registro : registros) {
            if (registro.getTipo().equals("Uso da CPU")) {
                indexUsoCpu = registros.indexOf(registro);
            }
        }

        // Adicionar alertas de uso da CPU à lista
        if (usoCpu != null) {
            if (usoCpu > 90) {
                alertas.add(new Alerta(idCompHasCompProcessador, registros.get(indexUsoCpu).getTipo(), registros.get(indexUsoCpu).getValor(), registros.get(indexUsoCpu).getValorFormatado(), registros.get(indexUsoCpu).getUnidade(), indexUsoCpu, "CRITICO", "CPU"));
            } else if (usoCpu > 80) {
                alertas.add(new Alerta(idCompHasCompProcessador, registros.get(indexUsoCpu).getTipo(), registros.get(indexUsoCpu).getValor(), registros.get(indexUsoCpu).getValorFormatado(), registros.get(indexUsoCpu).getUnidade(), indexUsoCpu, "INTERMEDIARIO", "CPU"));
            } else if (usoCpu > 70) {
                alertas.add(new Alerta(idCompHasCompProcessador, registros.get(indexUsoCpu).getTipo(), registros.get(indexUsoCpu).getValor(), registros.get(indexUsoCpu).getValorFormatado(), registros.get(indexUsoCpu).getUnidade(), indexUsoCpu, "MODERADO", "CPU"));
            }
        }

        // Iterar sobre os registros e alertas e inserir no banco de dados
        for (int i = 0; i < registros.size(); i++) {
            Registro registro = registros.get(i);
            if (registro.getValor() != null) {
                Integer idRegistro = conexao.inserirERetornarIdGerado("INSERT INTO registro (fkCompHasComp, tipo, dadoValor, dadoFormatado, dadoUnidade, dataHora) VALUES (?, ?, ?, ?, ?, NOW())", registro.getFkCompHasComp(), registro.getTipo(), registro.getValor(), registro.getValorFormatado(), registro.getUnidade());
                for (int j = 0; j < alertas.size(); j++) {
                    if (alertas.get(j).getIndexRegistro() == i) {
                        conexao.inserirERetornarIdGerado("INSERT INTO alerta (fkRegistro, grauAlerta, tipoComponente, dataHora) VALUES (?, ?, ?, NOW())", idRegistro, alertas.get(j).getGrauAlerta(), alertas.get(j).getTipoComponente());
                    }
                }
            }
        }

        System.out.println("Registros inseridos com sucesso!");
    }
}