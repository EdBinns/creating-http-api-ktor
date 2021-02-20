package com.jetbrains.handson.httpapi.routes

import com.jetbrains.handson.httpapi.models.Order
import com.jetbrains.handson.httpapi.models.OrderItem
import com.jetbrains.handson.httpapi.models.customerStorage
import com.jetbrains.handson.httpapi.models.orderStorage
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import sun.security.ec.point.ProjectivePoint

fun Route.orderRouting() {

    route("/order"){
        post("/addOrder") {
            val newOrder = call.receive<Order>()
            if(orderStorage.find { it.number == newOrder.number} != null) {
                call.respondText("Order is already exist", status = HttpStatusCode.BadRequest)
            }else{
                orderStorage.add(newOrder)
                call.respondText("Customer stored correctly", status = HttpStatusCode.Accepted)
            }
        }

        delete("/deleteOrder"){
            val id = call.request.queryParameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            //parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if(orderStorage.removeIf{ it.number == id}){
                call.respondText("Order removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }

        }

    }

}
fun Route.listOrdersRoute() {
    get("/orderList") {
        if (orderStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }
}
fun Route.getOrderRoute() {
    get("/order/{id}") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        call.respond(order)
    }
}
fun Route.totalizeOrderRoute() {
    get("/order/{id}/total") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        val total = order.contents.map { it.price * it.amount }.sum()
        call.respond(total)
    }
}

fun Route.deleteItemInOrder(){
    delete("/order/deleteItem"){
        val id = call.request.queryParameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val item =  call.request.queryParameters["item"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@delete call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        val itemList = order.contents
        if(itemList.removeIf { it.item == item}){
            call.respondText("Order Item  removed correctly", status = HttpStatusCode.Accepted)
        } else {
            call.respondText("Not Found XD ${order.number} $item", status = HttpStatusCode.NotFound)
        }
    }
}
fun Application.registerOrderRoutes() {
    routing {
        orderRouting()
        totalizeOrderRoute()
        getOrderRoute()
        listOrdersRoute()
        deleteItemInOrder()
    }
}
