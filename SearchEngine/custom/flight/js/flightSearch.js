$(document).ready(function() {
    var divClone = $("#flights").clone();
    $(".btnSubmit").click(function() {
      $("#flights").replaceWith(divClone);
      var direction = $("select#directionSelect").val();
      var url = 'http://flightinfo.phlapi.com/direction/' + direction;
		  $.ajax({
			  dataType: 'jsonp',
            type: 'GET',
             url: url,
         success: function(flightData) {
                   var userAirline = $("select#airlineSelect").val();
                   var userFlightNum = $("input#flightNumText").val();
                 	 var len = flightData.length;
                 	 var text = '';
                 	 
                 	 for(var i = 0; i < len; i++) {
                    if (userAirline != "" && userFlightNum != ""){

                      if (flightData[i].airline == userAirline && flightData[i].flightNumber == userFlightNum) {
                        text += buildFlightList(flightData[i]);
                      }
                    } 
                    else if (userAirline != "" && userFlightNum == "") {
                      if (flightData[i].airline == userAirline) {
                        text += buildFlightList(flightData[i]);
                      }
                    } 
                    else if (userAirline == "" && userFlightNum != "") {
                      if (flightData[i].flightNumber == userFlightNum) {
                        text += buildFlightList(flightData[i]);
                      }
                    } 
                    else {
                      text += buildFlightList(flightData[i]);
                    }
                 	 }  

                   if (text.length < 10) {
                    text = '<p>No flights found. Try refining your search criteria</p>';
                   } 
                    document.getElementById('flights').innerHTML = text;
                }
      });
    });

      function buildFlightList(fl) {
        var airport = '';
        var airline = '';
        var scheduledTime = '';
        var valScheduledTime = '';
        var estTime = '';
        var valEstTime = '';
        var entry = '';
        var statusClass = '';
        var estTimeClass = '';
        airline = airlineCode(fl.airline);
        if (fl.direction == 'Departure') {
          airport = 'Destination:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
          scheduledTime = 'Scheduled Departure: ';
          estTime = 'Estimated Departure: ';
        } else {
          airport = 'Origin:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
          scheduledTime = 'Scheduled Arrival:&nbsp;&nbsp;&nbsp;';
          estTime = 'Estimated Arrival:&nbsp;&nbsp;&nbsp;';
        }
        statusClass = setStatusClass(fl.status, valScheduledTime, valEstTime);
        valScheduledTime = timeFormat(fl.scheduledDateTime);
        valEstTime = timeFormat(fl.estimatedDateTime);

        if (valEstTime.slice(0, 5) < valScheduledTime.slice(0, 5) && valEstTime.slice(6, 17) <= valScheduledTime.slice(6, 17)) {
          estTimeClass = "estTimeEarly";
          statusClass = "statusOnTime";
        } else if (valEstTime.slice(0, 5) > valScheduledTime.slice(0, 5) || valEstTime.slice(6, 17) > valScheduledTime.slice(6, 17)) {
          estTimeClass = "estTimeLate";

        } else {
          estTimeClass = "estTime";
        }

        

        entry = '<ul class="flight"><li>Flight Number:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="flightNumber">' + fl.flightNumber + '</span></li><li>Airline:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="airline">' + airline + '</span></li> <li>Direction:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="direction">' + fl.direction + '</span></li> <li>Flight Type:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="flightType">' + fl.flightType +'</span></li> <li>' + airport +'<span  class="airport">'+ fl.airport + '</span></li> <li>'+ scheduledTime +'<span class="scheduledTime">' + valScheduledTime + '</span></li> <li>' + estTime + '<span class="'+ estTimeClass + '">' + valEstTime +'</span></li><li>Gate:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="gate">' + fl.gate + '</span></li> <li>Status:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="'+ statusClass +'">' + fl.status + '</span></li> <li>Terminal:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="terminal">' +fl.terminal + '</span></li> <li>Security Check:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="security">' + fl.security + '</span></li> <li>Garage:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="garage">' + fl.garage + '</span></li> <li>Septa Stop:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="septa">' + fl.septa + '</span></li></ul>';

        return entry;
      }

      function setStatusClass(status, scheduled, estimated) {

        s = '';

        if (status == "ON TIME" || status == "AT GATE") {
          s = "statusOnTime";
        }
        else if (status == "DELAYED") {
          s = "statusDelayed";
        }
        else if (status == "CANCELED") {
          s = "statusCanceled";
        }
        else if (status == "ARRIVED" || status == "DEPARTED") {
          s = "statusComplete"
        }
        else {
          s = "statusOther";
        }
        return s;
      }

			function timeFormat(time) {
        var currentTime = new Date();
        var timeZone = currentTime.getTimezoneOffset(); 
        timeZone = timeZone * 60000;
        var localTime = new Date((Date.parse(time) - timeZone));
				var d = localTime.toISOString().slice(0, 10);
				var t = localTime.toISOString().slice(11, 16);
				var formattedTime = t + '  ' + d;
				return formattedTime;
			}
      
      function airlineCode(code) {
       	var x = ''
       	switch(code)
       	{
       		case 'AS':
       			x = 'Alaska Airlines';
       			break;
       		case 'AC':
       			x = 'Air Canada';
       			break;
       		case 'QK':
       			x = 'Air Canada Jazz';
       			break;
       		case 'FL':
       			x = 'AirTran Airways';
       			break;
       		case 'AA':
       			x = 'American Airlines';
       			break;
       		case 'BA':
       			x = 'British Airways';
       			break;
       		case 'DL':
       			x = 'Delta Airlines';
       			break;
       		case 'F9':
       			x = 'Frontier Airlines';
       			break;
       		case 'LH':
       			x = 'Lufthansa Airlines';
       			break;
       		case 'WN':
       			x = 'Southwest Airlines';
       			break;
       		case 'NK':
       			x = 'Spirit Airlines';
       			break;
       		case 'UA':
       			x = 'United Airlines';
       			break;
       		case 'US':
       			x = 'US Airways';
       			break;
       		case 'VX':
       			x = 'Virgin America';
       			break;
       	}
       	return x;
      }

	});	