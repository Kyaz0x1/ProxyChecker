package net.kyaz0x1.proxychecker.manager;

import net.kyaz0x1.proxychecker.files.FileUtils;
import net.kyaz0x1.proxychecker.models.Proxy;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ProxyManager {

    private static ProxyManager INSTANCE;

    private final ExecutorService PROXY_CHECKER;
    private final ExecutorService PROXY_WRITER;

    private final Set<Proxy> proxies;
    private final Set<Proxy> proxiesWork;
    private final Set<Proxy> proxiesDead;

    private final AtomicInteger proxiesChecked;

    private ProxyManager(){
        this.proxies = new HashSet<>();
        this.proxiesWork = new HashSet<>();
        this.proxiesDead = new HashSet<>();
        this.proxiesChecked = new AtomicInteger();

        this.PROXY_CHECKER = Executors.newFixedThreadPool(10);
        this.PROXY_WRITER = Executors.newSingleThreadExecutor();
    }

    public Set<Proxy> loadProxies(File file){
        try {
            final List<String> lines = Files.readAllLines(Paths.get(file.toURI()));
            for(String line : lines){
                if(line.isEmpty())
                    continue;
                if(!line.contains(":"))
                    continue;

                final String[] splitted = line.split(":");

                final String host = splitted[0];

                if(!isAddress(host))
                    continue;

                final int port = Integer.parseInt(splitted[1]);

                final Proxy proxy = new Proxy(host, port);
                proxies.add(proxy);
            }
            return proxies;
        }catch(IOException e) {
            e.printStackTrace();
            return proxies;
        }
    }

    public void saveProxies(){
        PROXY_WRITER.submit(() -> {
            final File fileWorks = new File("proxies-works.txt");
            final File fileDeads = new File("proxies-deads.txt");

            try{
                if(!fileWorks.exists()){
                    System.out.println("Criando arquivo para salvar as proxies encontradas...");
                    fileWorks.createNewFile();
                }
                if(!fileDeads.exists()){
                    System.out.println("Criando arquivo para salvar as proxies não encontradas...");
                    fileDeads.createNewFile();
                }
            }catch(IOException e) {
                e.printStackTrace();
            }

            FileUtils.write(proxiesWork.stream()
                    .map(Proxy::toString)
                    .collect(Collectors.joining("\n")),
                    fileWorks
            );

            System.out.println("Todas as proxies encontradas foram salvadas com sucesso!");

            FileUtils.write(proxiesDead.stream()
                    .map(Proxy::toString)
                    .collect(Collectors.joining("\n")),
                    fileDeads
            );

            System.out.println("Todas as proxies não encontradas foram salvadas com sucesso!");
        });
    }

    public boolean check(Proxy proxy){
        final Future<Boolean> result = PROXY_CHECKER.submit(() -> {
            try{
                final InetAddress addr = InetAddress.getByName(proxy.getHost());
                return addr.isReachable(5000);
            }catch(IOException e){
                try{
                    final Socket socket = new Socket(proxy.getHost(), proxy.getPort());
                    final InetSocketAddress addr = new InetSocketAddress("http://google.com", 80);
                    socket.connect(addr, 10000);
                    return socket.isConnected();
                }catch(IOException e2){
                    return false;
                }
            }
        });

        proxiesChecked.incrementAndGet();

        try {
            if(result.get()){
                proxiesWork.add(proxy);
            }else{
                proxiesDead.add(proxy);
            }
            return result.get();
        }catch(InterruptedException | ExecutionException e) {
            proxiesDead.add(proxy);
            return false;
        }
    }

    public boolean isAddress(String value){
        final String REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

        final Pattern pattern = Pattern.compile(REGEX);
        final Matcher matcher = pattern.matcher(value);

        return matcher.find();
    }

    public Set<Proxy> getProxiesWork() {
        return proxiesWork;
    }

    public Set<Proxy> getProxiesDead() {
        return proxiesDead;
    }

    public AtomicInteger getProxiesChecked() {
        return proxiesChecked;
    }

    public static ProxyManager getInstance(){
        if(INSTANCE == null){
            synchronized(ProxyManager.class){
                if(INSTANCE == null){
                    INSTANCE = new ProxyManager();
                }
            }
        }
        return INSTANCE;
    }

}