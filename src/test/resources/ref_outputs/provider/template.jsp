

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<!--$beginparam$title$-->
			<title>Exemple de template (titre = bloc variable)</title>
		<!--$endparam$title$-->
	</head>
	<body style="background-color: aqua">
		<div>
			<img src="images/smile.jpg" />&lt;-- Image g�r�e par le provider
		</div>
		<div style="border: 1px solid red">
			<!--$beginparam$param1$-->Bloc variable 1<!--$endparam$param1$--><br />
		</div>
		<br />
		<div style="border: 1px solid green">
			Texte a remplacer par le tag replace (original : Lorem&nbsp;ipsum) :<br />
			Lorem ipsum
		</div>	
		<div style="border: 1px solid red">
			<!--$beginparam$param2$-->Bloc variable 2<!--$endparam$param2$-->
		</div>
		<div>
			<img src="images/smile.jpg" />
		</div>
		<div style="border: 1px solid green">
			Autre texte a remplacer par le tag replace (original : Lorem&nbsp;ipsum) :<br />
			Lorem ipsum
		</div>	
	</body>
</html>