package com.example.seath.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import java.awt.Desktop;
import java.net.URI;

@Configuration
@Profile("dev")
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = environment.getProperty("local.server.port", "8080");
        String url = "http://localhost:" + port;
        System.out.println("-------------------------------------------------");
        System.out.println("Aplicação iniciada! Abrindo navegador em: " + url);
        System.out.println("-------------------------------------------------");
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println("Erro ao tentar abrir o navegador: " + e.getMessage());
            }
        } else {
            System.err.println("Não foi possível abrir o navegador. Ambiente gráfico não suportado.");
        }
    }
}
