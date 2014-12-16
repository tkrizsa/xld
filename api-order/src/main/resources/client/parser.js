//@xld-parser
xld.mainScope.urlParsers.push(function(url, urla) {		
	if (urla[0] == 'orders' && urla[1]) {
		return {
			url 		: url,
			template 	: '/templates/order',
			params 		: {
				orderId : urla[1]
			}
		}
	
	} else if (
		url == '/orders' 						||
		false) 
	{
		return {
			url 	 : url,
			template : '/templates' + url
		}
	}
	
});

