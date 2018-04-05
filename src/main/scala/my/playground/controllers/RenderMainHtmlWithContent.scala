package my.playground.controllers

import java.net.{Inet4Address, NetworkInterface}

import akka.http.scaladsl.model._
import my.playground.AkkaArtifacts
import my.playground.utils.Handlebars
import org.json4s._


class RenderMainHtmlWithContent (
                                  akkaArtifacts: AkkaArtifacts,
                                  handlebars : Handlebars = Handlebars
                                ) {

  import RenderMainHtmlWithContent._

  protected def failResponse(msg:String) = HttpResponse(
      entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,msg),
      status = StatusCodes.InternalServerError
    )

  def render(templateFile:String, values: JValue, title:String = ""): HttpResponse =
    try HttpResponse(
      entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,handlebars("main.html",MainItems(
        content = handlebars(templateFile,values),
        title   = title,
        port    = akkaArtifacts.httpServerPort.map(_.toString).getOrElse("N/A"),
        host    = netInterfaces
      ).toJson))
    )
    catch {
      case Handlebars.HandlebarsException(msg) => failResponse(msg)
    }

  def renderFromString(template:String, values: JValue, title:String = ""): HttpResponse =
     try HttpResponse(
      entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,handlebars("main.html",MainItems(
        content = handlebars.render(template,values),
        title   = title,
        port    = akkaArtifacts.httpServerPort.map(_.toString).getOrElse("N/A"),
        host    = netInterfaces
      ).toJson))
    )
    catch {
      case Handlebars.HandlebarsException(msg) => failResponse(msg)
    }

}

object RenderMainHtmlWithContent {

  case class MainItems(content:String, title:String, host:String, port: String)
  {
    implicit val formats = org.json4s.DefaultFormats
    def toJson: JValue =  Extraction.decompose(this)
  }

  def apply(akkaArtifacts: AkkaArtifacts) = new RenderMainHtmlWithContent(akkaArtifacts)

  protected def evalIFaces:String = {
    var interfaces = ""
    val ifaces = NetworkInterface.getNetworkInterfaces()
    while (ifaces.hasMoreElements()) {
      val iface = ifaces.nextElement()
      val addresses = iface.getInetAddresses()

      while (addresses.hasMoreElements()) {
        val addr = addresses.nextElement()
        if (addr.isInstanceOf[Inet4Address] && !addr.isLoopbackAddress()) {
          interfaces += addr
        }
      }
    }
    interfaces
  }

  val netInterfaces = evalIFaces

}