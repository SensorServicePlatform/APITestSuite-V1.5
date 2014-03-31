/**
 * Copyright (c) 2013 Carnegie Mellon University Silicon Valley. 
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available
 * under the terms of dual licensing(GPL V2 for Research/Education
 * purposes). GNU Public License v2.0 which accompanies this distribution
 * is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * 
 * Please contact http://www.cmu.edu/silicon-valley/ for more specific
 * information.
 */

package unit

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

/**
 * Class that runs Load and Performance testing for GET operations
 * on the Sensor Data Service Platform V1.5
 *
 * Author: Surya Kiran
 */
class APISimulationV15 extends Simulation {
	// Perpare the HTTP config we would like to invoke the request on  
	val httpConf_v15 = http.baseURL("http://einstein.sv.cmu.edu:9000")
		.acceptCharsetHeader("utf-8")
		.acceptHeader("text/html")
		.disableFollowRedirect

	// Difference headers for the requests.		
	val headers_1 = Map("Keep-Alive" -> "115")
	val headers_2 = Map("Accept" -> "application/json", "Keep-Alive" -> "115")

	// Build up the scenario on which we would like to perform 
	// Load testing and gauge it's performance.
	val scn = scenario("Sensor Data Platform Service v1.5")
		.group("Get Devices v1.5") {
			exec(
				http("Get_Devices_R3")
					.get("/getAllDevices/json")
					.headers(headers_1)
					.check(status.is(200)))
				.pause(0 milliseconds, 100 milliseconds)
				.exec(
					http("Get_Devices_R4")
						.get("/getAllDevices/csv")
						.headers(headers_1))
		}
	// Pause between the requests for 100 milliseconds.
	// And perform another round of testing on different
	// API methods	
		.pause(0 milliseconds, 100 milliseconds)
		.group("Get Sensor Types") {
			exec(
				http("Get_SensorTypes_R3")
					.get("/getAllSensorTypes/json")
					.headers(headers_1)
					.check(status.is(200)))
				.pause(0 milliseconds, 100 milliseconds)
				.exec(
					http("Get_SensorTypes_R4")
						.get("/getAllSensorTypes/csv")
						.headers(headers_1))
		}
		
	// Setup and execute the scenario that is prepared above.
	// Run it simulating 100 users accessing the API in 
	// a time range of 10 seconds. 
	setUp(scn.inject(ramp(100 users) over (10 seconds)))
		.protocols(httpConf_v15)
		.assertions(
			global.successfulRequests.percent.is(100),
			details("Get Devices v1.5" / "Get_Devices_R3").responseTime.max.lessThan(20000))
}
