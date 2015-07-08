/*
 *
    COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute
    these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing
    application programs conforming to the application programming interface for the operating platform for which the sample code is written.
    Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES,
    EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
    FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
    INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE.
    IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.

 */

var busyIndicator = null;
var topic = "";

function wlCommonInit(){
	busyIndicator = new WL.BusyIndicator();
	loadFeeds();
}

$('#topic').on("change",function(){
	topic = this.value;
	loadFeeds();
});

function loadFeeds(){
	busyIndicator.show();
	
	/*
	 * The REST API works with all adapters and external resources, and is supported on the following hybrid environments: 
	 * iOS, Android, Windows Phone 8, Windows 8. 
	 * If your application supports other hybrid environments, see the tutorial for MobileFirst 6.3.
	 */
	var resourceRequest = new WLResourceRequest("/adapters/RSSAdapter/", WLResourceRequest.GET);
	resourceRequest.setQueryParameter("topic", topic);
	resourceRequest.send().then(
			loadFeedsSuccess,
			loadFeedsFailure
	);
}


function loadFeedsSuccess(result){
	WL.Logger.debug("Feed retrieve success");
	busyIndicator.hide();
	WL.Logger.debug(JSON.stringify(result));
	if (result.responseJSON.rss.channel.item.length>0) 
		displayFeeds(result.responseJSON.rss.channel.item);
	else 
		loadFeedsFailure();
}

function loadFeedsFailure(result){
	WL.Logger.error("Feed retrieve failure");
	busyIndicator.hide();
	WL.SimpleDialog.show("RSS Reader", "Service not available. Try again later.", 
			[{
				text : 'Reload',
				handler : WL.Client.reloadApp 
			},
			{
				text: 'Close',
				handler : function() {}
			}]
		);
}

function displayFeeds(items){
	var ul = $('#itemsList');
	ul.empty();
	for (var i = 0; i < items.length; i++) {
		var li = $('<li/>').text(items[i].title);
		var pubDate = $('<div/>', {
			'class': 'pubDate'
		}).text(items[i].pubDate);

		li.append(pubDate);
		
		ul.append(li);
	}
}