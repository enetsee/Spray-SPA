package com.example


import actors.{ServiceActor, StorageActor}
import akka.actor._

import slick.driver.H2Driver
import slick.session.Database

import spray.can.server.HttpServer
import spray.io.{SingletonHandler, IOExtension}

import storage.Store


object Boot extends App {

  val system = ActorSystem("example")
  import system.log
  log.info(s"Starting Actor system '${system.name}'.")

  private val ioBridge = IOExtension(system).ioBridge()


  log.info("Starting storage actor.")
  val store = new Store(H2Driver, Database.forURL("jdbc:h2:tcp://localhost/~/example", driver = "org.h2.Driver", user="sa"))
  store.createDB(store.session)
  val storage = system.actorOf(Props( new StorageActor(store) ),"storage")


  log.info("Starting service actor and http server.")
  val service = system.actorOf(Props( new ServiceActor(storage) ),"service")

  val httpServer = system.actorOf(
    Props(new HttpServer(ioBridge,SingletonHandler(service))),
    name ="http-server"
  )
  httpServer ! HttpServer.Bind(SiteSettings.Interface,SiteSettings.Port)

}
