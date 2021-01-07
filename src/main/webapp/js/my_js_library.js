function showForm(target) {
	var elem = document.getElementById(target).style.display;
	if(elem == "none") {
		elem = "";	
	} else {
		elem = "none";	
	}
	document.getElementById(target).style["display"] = elem;
		}

//var counter = 0; 

function addRow() {
	var request = new XMLHttpRequest();
	request.open('GET', "/logiweb/getCityAndCargoInfo");
	request.responseType = 'json';
	request.onload = function() {
		var text = JSON.stringify(request.response);
		var parsed = JSON.parse(text);
		var cities = parsed[0];
		var cargo = parsed[1];
		var options_city = '';
		var options_cargo = '';
		var selectedCityOptions = [];
		var selectedCargoOptions = [];
		var selectedStatusOptions = [];
		for(var i = 0; i < counter; i++) {
			selectedCityOptions.push(document.getElementById("city" + i).selectedIndex);
			selectedCargoOptions.push(document.getElementById("cargo" + i).selectedIndex);
			selectedStatusOptions.push(document.getElementById("status" + i).selectedIndex);
		}
		for(var i = 0; i < cities.length; i++) {
			options_city += "<option value = \"" + cities[i].currentCity + "\">" + cities[i].currentCity + "</option>";
		}
		for(var i = 0; i < cargo.length; i++) {
			options_cargo += "<option value = \"" + cargo[i].id + "\"" + ">" + cargo[i].id + ": " + cargo[i].name + "</option>" 
		}
		document.getElementById('rows').innerHTML = document.getElementById('rows').innerHTML 
		+ "<div id = \"div" + counter + "\">" 
		+ "<label>City</label> "
		+ "<select id = \"city" + counter + "\" name = \"list[" + counter + "].cityName\">"  
		+ options_city 
		+ "</select> " 
		+ "<label>Cargo</label> " 
		+ "<select id = \"cargo" + counter + "\" name =\"list[" + counter + "].cargoId\">"   
		+ options_cargo
		+ "</select> "
		+ "<label>Status</label> " 
		+ "<select id = \"status" + counter + "\" name = \"list[" + counter + "].isLoading\">" 
		+	"<option value = \"true\">Load</option>" 
		+	"<option value = \"false\">Drop</option>"  
		+ "</select> "
		+ "<a id=\"a" + counter + "\" href=\"javascript: deleteRow(" + counter + ");\">Delete this row </a>"
		+ "<br>"
		+ "</div>";
		for(var i = 0; i < counter; i++) {
			document.getElementById("city" + i).options[selectedCityOptions[i]].selected = true;
			document.getElementById("cargo" + i).options[selectedCargoOptions[i]].selected = true;
			document.getElementById("status" + i).options[selectedStatusOptions[i]].selected = true;
		}
		counter++;
		return true;
	};
	request.send();
}

function deleteRow(x) {
	var elem = document.getElementById("div" + x);
	elem.parentNode.removeChild(elem);
	for(var i = x + 1; i < counter; i++) {
		document.getElementById("a" + i).setAttribute("href", "javascript: deleteRow(" + (i - 1) + ")");
		document.getElementById("a" + i).setAttribute("id", "a" + (i - 1));
		document.getElementById("div" + i).setAttribute("id", "div" + (i - 1));
		document.getElementById("city" + i).setAttribute("id", "city" + (i - 1));
		document.getElementById("cargo" + i).setAttribute("id", "cargo" + (i - 1));
		document.getElementById("status" + i).setAttribute("id", "status" + (i - 1));
	}
	counter--;
}