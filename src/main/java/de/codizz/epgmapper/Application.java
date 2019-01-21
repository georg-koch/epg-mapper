/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */
package de.codizz.epgmapper;

import static spark.Spark.*;
d
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

@Slf4j
public class Application {

   private static final String CONFIG_FILE = "app.properties";

   static final Properties CONFIG = new Properties();

   public static void main( String[] args ) throws Exception {
      loadConfiguration();
      configureServer();
      printEnvVariables();

      before( Application::setCorrelationId );
      before( Application::printRequest );

      path( "/api/1/epg/xmltv", () -> {
         get( "/next/download", XmlTvController.create() );
         get( "/prev/download", XmlTvController.create() );
      } );

      after( Application::printResponse );
      afterAfter( Application::removeCorrelationId );

      // -----------------------
      notFound( "<html><body><h1>You are wrong here</h1></body></html>" );
      internalServerError( "<html><body><h1>I'm tired, leave me alone</h1></body></html>" );
      exception( RuntimeException.class, ( exception, request, response ) -> log.error( "Unhandled exception", exception ) );
   }

   private static void removeCorrelationId( Request request, Response response ) {
      MDC.remove( "correlationId" );
   }

   private static void setCorrelationId( Request request, Response response ) {
      MDC.put( "correlationId", UUID.randomUUID().toString() );
   }

   private static void printResponse( Request request, Response response ) {
      log.info( "Response: {}", response.status() );
      log.info( "ResponseHeaders: " );
      if ( response.raw().getHeaderNames() != null ) {
         response.raw().getHeaderNames().forEach( header -> log.info( "  {}: {}", header, response.raw().getHeader( header ) ) );
      }
   }

   private static void printRequest( Request request, Response response ) {
      log.info( "Request: {} {} {}", request.requestMethod(), request.url(), request.protocol() );
      log.info( "RequestHeaders: " );
      if ( request.headers() != null ) {
         request.headers().forEach( header -> log.info( "  {}: {}", header, request.headers( header ) ) );
      }
   }

   private static void printEnvVariables() {
      System.getenv().forEach( ( key, value ) -> log.info( "APP-ENV {} = {}", key, value ) );

   }

   private static void configureServer() {
      String envVar = System.getenv( "PLATFORM" );
      if ( StringUtils.isNotEmpty( envVar ) && "gae".equals( envVar ) ) {
         port( 8080 );
      } else {
         port( Integer.parseInt( CONFIG.getProperty( "server.port", "8080" ) ) );
      }
   }

   private static void loadConfiguration() throws IOException {
      try ( InputStream inputStream = ClassLoader.getSystemResourceAsStream( CONFIG_FILE ) ) {
         CONFIG.load( inputStream );
      } catch ( IOException e ) {
         log.error( "can't load file " + CONFIG_FILE, e );
         throw e;
      }
   }
}
