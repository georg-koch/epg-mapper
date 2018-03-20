/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */

package de.codizz.epgmapper;

public class FileDownloadException extends RuntimeException {

   public FileDownloadException() {
      super();
   }

   public FileDownloadException( String s ) {
      super( s );
   }

   public FileDownloadException( String message, Throwable cause ) {
      super( message, cause );
   }

   public FileDownloadException( Throwable cause ) {
      super( cause );
   }
}
