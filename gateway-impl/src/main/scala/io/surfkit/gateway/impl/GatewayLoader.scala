package io.surfkit.gateway.impl

import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import io.surfkit.gateway.api.GatewayService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._
import io.surfkit.projectmanager.api.ProjectManagerService

class GatewayLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new GatewayApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    println("\n\n**** IN DEV MODE *****\n\n")
    new GatewayApplication(context) with LagomDevModeComponents
  }

  override def describeService = Some(readDescriptor[GatewayService])
}

abstract class GatewayApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  lazy val projectService = serviceClient.implement[ProjectManagerService]

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[GatewayService](wire[GatewayServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = UserSerializerRegistry

  // Register the gateway persistent entity
  persistentEntityRegistry.register(wire[UserEntity])
}
