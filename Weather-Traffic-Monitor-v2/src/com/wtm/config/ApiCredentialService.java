package com.wtm.config;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * Stores API credentials separately from ordinary dashboard configuration.
 *
 * This is not intended to be a cryptographic vault; it keeps secrets out of
 * the human-readable site configuration and applies owner-only POSIX file
 * permissions where the operating system supports them. For a future
 * network-hosted deployment, credentials should move to environment variables
 * or the host operating system's secret store.
 */
public final class ApiCredentialService {
    private static final String FILE_NAME="credentials.properties";
    private ApiCredentialService() {}

    private static Path file(){ return ConfigService.appDataDir().resolve(FILE_NAME); }

    public static void loadInto(AppConfig cfg){
        try{
            Files.createDirectories(ConfigService.appDataDir());
            if(!Files.exists(file())) return;
            Properties p=new Properties();
            try(InputStream in=Files.newInputStream(file())){ p.load(in); }
            cfg.weatherApiKey=p.getProperty("weatherApiKey","").trim();
            cfg.tomTomApiKey=p.getProperty("tomTomApiKey","").trim();
            cfg.sportsApiKey=p.getProperty("sportsApiKey",cfg.sportsApiKey).trim();
        }catch(Exception ex){
            System.err.println("Credential load failed: "+ex.getMessage());
        }
    }

    public static void saveFrom(AppConfig cfg){
        try{
            Files.createDirectories(ConfigService.appDataDir());
            Properties p=new Properties();
            p.setProperty("weatherApiKey",safe(cfg.weatherApiKey));
            p.setProperty("tomTomApiKey",safe(cfg.tomTomApiKey));
            p.setProperty("sportsApiKey",safe(cfg.sportsApiKey));
            try(OutputStream out=Files.newOutputStream(file())){
                p.store(out,"Weather & Traffic Monitor API credentials - keep private");
            }
            restrictPermissions();
        }catch(IOException ex){
            throw new RuntimeException("Unable to save API credentials",ex);
        }
    }

    private static String safe(String v){ return v==null?"":v; }

    private static void restrictPermissions(){
        try{
            Set<PosixFilePermission> perms=EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file(),perms);
        }catch(Exception ignored){
            // Windows and some file systems do not expose POSIX permissions.
        }
    }
}
