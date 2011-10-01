$(document).ready(function() {
	draw('#week', myData.Week);
	draw('#month', myData.Month);
	draw('#year', myData.Year);
	draw('#overall', myData.Overall);
});

function draw(id, data) {
	$.plot($(id), data, {
		series : {
			pie : {
				show : true,
				radius : .75
			}
		},
		legend : {
			show : true,
			radius : 1,
			position : 'nw'
		}
	});
}

function showaccount(acct) {
	
	document.location =   "history?id=" + acct;
}