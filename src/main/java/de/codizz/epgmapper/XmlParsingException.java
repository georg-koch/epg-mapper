/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */

package de.codizz.epgmapper;

public class XmlParsingException extends RuntimeException {

   public XmlParsingException() {
      super();
   }

   public XmlParsingException( String s ) {
      super( s );
   }

   public XmlParsingException( String message, Throwable cause ) {
      super( message, cause );
   }

   public XmlParsingException( Throwable cause ) {
      super( cause );
   }
}