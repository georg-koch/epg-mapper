/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */

package de.codizz.epgmapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FileUtil {

   private FileUtil() {
      // NOP
   }

   public static File unpack( File file, File targetFile ) {
      log.debug( "try to unpack the file {}", file );
      try ( InputStream inputStream = Files.newInputStream( file.toPath() );
            BufferedInputStream bufferedInputStream = new BufferedInputStream( inputStream );
            GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream( bufferedInputStream );
            OutputStream outputStream = Files.newOutputStream( targetFile.toPath() ) ) {
         byte[] buffer = new byte[2048];
         int n;
         while ( -1 != (n = gzipCompressorInputStream.read( buffer )) ) {
            outputStream.write( buffer, 0, n );
         }
      } catch ( IOException e ) {
         throw new FileAcessException( "can't unpack the file " + file.getPath(), e );
      } finally {
         deleteFile( file );
      }
      return targetFile;
   }

   public static File pack( File file, File targetFile ) {
      log.debug( "try to pack the file {}", file );
      try ( InputStream inputStream = Files.newInputStream( file.toPath() );
            OutputStream outputStream = Files.newOutputStream( targetFile.toPath() );
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream( outputStream );
            GzipCompressorOutputStream gzipCompressorOutputStream = new GzipCompressorOutputStream( bufferedOutputStream ) ) {
         byte[] buffer = new byte[2048];
         int n = 0;
         while ( -1 != (n = inputStream.read( buffer )) ) {
            gzipCompressorOutputStream.write( buffer, 0, n );
         }
      } catch ( IOException e ) {
         throw new FileAcessException( "can't pack the file " + file.getPath(), e );
      } finally {
         deleteFile( file );
      }
      return targetFile;
   }

   public static void deleteFile( File file ) {
      log.debug( "try to delete the file {}", file );
      if ( file.exists() ) {
         try {
            Files.delete( file.toPath() );
         } catch ( IOException e ) {
            throw new FileAcessException( "can't delete the file " + file.toPath(), e );
         }
      }
   }

   public static String fileNameWithoutEnding( File file ) {
      String fileName = file.getName();
      int indexOfDot = fileName.lastIndexOf( '.' );
      if ( indexOfDot > 0 ) {
         return fileName.substring( 0, indexOfDot );
      } else {
         return fileName;
      }
   }
}
