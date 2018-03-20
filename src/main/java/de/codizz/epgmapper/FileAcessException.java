/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */

package de.codizz.epgmapper;

public class FileAcessException extends RuntimeException {

   public FileAcessException() {
      super();
   }

   public FileAcessException( String s ) {
      super( s );
   }

   public FileAcessException( String message, Throwable cause ) {
      super( message, cause );
   }

   public FileAcessException( Throwable cause ) {
      super( cause );
   }
}