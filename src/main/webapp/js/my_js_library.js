function showForm(target)
		{
			var elem = document.getElementById(target).style.display;
			if(elem == "none")
			{
				elem = "";	
			}
			else
			{
				elem = "none";	
			}
			document.getElementById(target).style["display"] = elem;
		}