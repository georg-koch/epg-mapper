/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */

package de.codizz.epgmapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.UUID;

import org.apache.http.client.fluent.Request;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import lombok.extern.slf4j.Slf4j;
import spark.Response;
import spark.Route;

@Slf4j
public class XmlTvController implements Route {

   private XmlTvController() {
      // NOP
   }

   @Override
   public Object handle( spark.Request request, Response response ) throws Exception {
      File tmpGzFile = loadTeleguideFile();
      File tmpXmlFile = FileUtil.unpack( tmpGzFile, new File( FileUtil.fileNameWithoutEnding( tmpGzFile ) + ".unpacked" ) );
      File xmlFile = manipulate( tmpXmlFile, new File( FileUtil.fileNameWithoutEnding( tmpXmlFile ) ) );
      File gzFile = FileUtil.pack( xmlFile, new File( Application.CONFIG.getProperty( "teleguide.xmltv.fileName" ) ) );

      response.raw().setHeader( "Accept-Ranges", "bytes" );
      response.raw().setHeader( "Content-Disposition", String.format( "attachment; filename=\"%s\"", gzFile.getName() ) );
      response.raw().setContentType( "application/octet-stream" );
      response.raw().setContentLength( (int) gzFile.length() );

      byte[] bytes = Files.readAllBytes( gzFile.toPath() );
      response.raw().getOutputStream().write( bytes );
      response.raw().getOutputStream().flush();
      response.raw().getOutputStream().close();

      return response.raw();
   }

   private File loadTeleguideFile() {
      log.debug( "try to load teleguide file" );
      File file = new File( UUID.randomUUID().toString() + ".gz" );
      try {
         Request.Get( Application.CONFIG.getProperty( "teleguide.xmltv.download.next" ) ).execute().saveContent( file );
      } catch ( IOException e ) {
         throw new FileDownloadException( "can't load the teleguide file.", e );
      }
      return file;
   }

   private File manipulate( File file, File targetFile ) {
      log.debug( "try to manipulate the file {}", file );
      try ( InputStream inputStream = Files.newInputStream( file.toPath() );
            OutputStream outputStream = Files.newOutputStream( targetFile.toPath() );
            BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( outputStream ) ) ) {
         SAXBuilder saxBuilder = new SAXBuilder();
         Document document = saxBuilder.build( inputStream );
         Element rootElement = document.getRootElement();
         Iterator<Element> channelElements = rootElement.getChildren( "channel" ).iterator();
         while ( channelElements.hasNext() ) {
            Element channelElement = channelElements.next();
            int channelId = channelElement.getAttribute( "id" ).getIntValue();
            if ( channelId == 326 ) {
               Element displayName = channelElement.getChild( "display-name" );
               log.info( displayName.getText() );
               // TODO configurable replacement
               displayName.setText( "РБК-ТВ" );
            }
         }
         XMLOutputter xmlOutput = new XMLOutputter();
         xmlOutput.output( document, bufferedWriter );
         // https://www.tutorialspoint.com/java_xml/java_jdom_modify_document.htm
         // id = 326 РБК
      } catch ( IOException e ) {
         throw new FileAcessException( "can't open the file " + file.getPath(), e );
      } catch ( JDOMException e ) {
         throw new XmlParsingException();
      } finally {
         FileUtil.deleteFile( file );
      }
      return targetFile;
   }

   static XmlTvController create() {
      return new XmlTvController();
   }
}