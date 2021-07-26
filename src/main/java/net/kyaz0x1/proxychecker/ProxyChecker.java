package net.kyaz0x1.proxychecker;

import net.kyaz0x1.proxychecker.files.FileUtils;
import net.kyaz0x1.proxychecker.files.type.FileExtensionType;
import net.kyaz0x1.proxychecker.manager.ProxyManager;
import net.kyaz0x1.proxychecker.models.Proxy;

import java.io.File;
import java.util.Scanner;
import java.util.Set;

public class ProxyChecker {

    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("Argumentos inválidos! Use: java -jar ProxyChecker.jar <file>");
            return;
        }

        final File file = new File(args[0]);

        if(!file.exists()){
            System.out.println("O arquivo informado não existe! Certifique-se de que o local foi informado corretamente.");
            return;
        }

        if(!FileUtils.hasExtension(file, FileExtensionType.TEXT_FILE)){
            System.out.println("O arquivo informado não é um arquivo de texto!");
            return;
        }

        final ProxyManager proxyManager = ProxyManager.getInstance();
        final Set<Proxy> proxies = proxyManager.loadProxies(file);

        if(proxies.isEmpty()){
            System.out.println("Não existe nenhuma proxy para checar!");
            return;
        }

        System.out.println("Proxies: " + proxies.size());

        for(Proxy proxy : proxies){
            final boolean result = proxyManager.check(proxy);
            final String proxiesChecked = String.format("(%d/%d)",
                    proxyManager.getProxiesChecked().get(),
                    proxies.size()
            );

            if(result){
                System.out.printf("%s [+] %s é uma proxy...\n", proxiesChecked, proxy);
            }else{
                System.out.printf("%s [-] %s não é uma proxy...\n", proxiesChecked, proxy);
            }
        }

        System.out.printf(">> Proxies: %d | Works: %d | Deads: %d\n",
                proxies.size(),
                proxyManager.getProxiesWork().size(),
                proxyManager.getProxiesDead().size()
        );

        System.out.println("Deseja salvar as proxies? Digite \"y\" para sim ou \"n\" para não.");

        try(Scanner in = new Scanner(System.in)){
            final String option = in.nextLine();
            switch(option){
                case "y":
                    proxyManager.saveProxies();
                    break;
                case "n":
                    System.out.println("As proxies não serão salvadas! Fechando programa...");
                    break;
                default:
                    System.out.println("Por padrão, as proxies não serão salvadas. Fechando programa...");
            }
        }
    }

}