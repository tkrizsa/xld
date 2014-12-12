//@xld-parser
xld.mainScope.urlParsers.push(function(url, urla) {		
	if (urla[0] == 'articles' && urla[1]) {
		return {
			url 		: url,
			template 	: '/templates/article',
			params 		: {
				articleId : urla[1]
			}
		}
	
	} else if (
		url == '/articles' 						||
		false) 
	{
		return {
			url 	 : url,
			template : '/templates' + url
		}
	}
	
});

