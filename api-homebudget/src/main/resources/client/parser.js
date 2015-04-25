//@xld-parser
xld.mainScope.urlParsers.push(function(url, urla) {		
	if (urla[0] == 'expensekinds' && urla[1]) {
		return {
			url 		: url,
			template 	: '/templates/expensekind',
			params 		: {
				expenseKindId : urla[1]
			}
		}
	
	} else if (urla[0] == 'expenses' && urla[1]) {
		return {
			url 		: url,
			template 	: '/templates/expense',
			params 		: {
				expenseId : urla[1]
			}
		}
	
	} else if (
		url == '/expenses' 						||
		url == '/expensekinds' 					||
		false) 
	{
		return {
			url 	 : url,
			template : '/templates' + url
		}
	}
	
});

