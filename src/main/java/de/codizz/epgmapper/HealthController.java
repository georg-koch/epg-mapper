/*
 * Copyright (c) 2018. Georg Koch <info@codizz.de>
 */

package de.codizz.epgmapper;

import spark.Request;
import spark.Response;
import spark.Route;

class HealthController implements Route {

   private HealthController() {
      // NOP
   }

   @Override
   public Object handle( Request request, Response response ) throws Exception {
      return "check";
   }

   static HealthController create() {
      return new HealthController();
   }
}