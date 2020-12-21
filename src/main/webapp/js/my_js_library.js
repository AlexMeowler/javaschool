function showForm()
		{
			var elem = document.getElementById("form").style.display;
			if(elem == "none")
			{
				elem = "";	
			}
			else
			{
				elem = "none";	
			}
			document.getElementById("form").style["display"] = elem;
		}