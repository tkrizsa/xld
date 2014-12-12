//@xld-parser
xld.mainScope.urlParsers.push(function(url, urla) {		
	if (urla[0] == 'partners' && urla[1]) {
		return {
			url 		: url,
			template 	: '/templates/partner',
			params 		: {
				partnerId : urla[1]
			}
		}
	
	} else if (
		url == '/' 						||
		url == '/home' 					||
		url == '/about' 				||
		url == '/partners' 				||
		url == '/partners/new' 			||
		false) 
	{
		return {
			url 	 : url,
			template : '/templates' + (url == '/' || url == '' ? '/home' : url)
		}
	}
	
});

