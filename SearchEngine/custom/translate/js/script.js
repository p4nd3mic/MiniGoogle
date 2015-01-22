var translate = function(){

	var translate_api = {
		
		settings: {
		
		},

		log : [],

		defaults : {
			text : 'ana are mere',
			loops : 3,
			currentBlockIndex : 1,
			fromLanguage : 'ro',
			toLanguage : 'en',
			success : function(){}
		},
		translate : function(options){
			translate_api.settings = $.extend(translate_api.defaults, options);
			this.settings.currentBlockIndex = 1;
			$('#loader').css('display','block');
			this._trans();
		},

		_getMail : function(){
			return new Date().getTime() + "a@mofo.mofo";
		},
		_trans : function(){
			$.get( 'http://mymemory.translated.net/api/get', {
					de : translate_api._getMail(),
					q : translate_api.settings.text,
					langpair : translate_api.settings.fromLanguage + '|' + translate_api.settings.toLanguage
			}) 
			.done(function(data){
				if(data.responseStatus != '200')
					// error warning
					return;

				/*console.log("De Tradus: " + data.matches[0].segment);
				console.log("Tradus: " + data.matches[0].translation);*/
				

				translate_api.settings.text = translate_api._decode( data.matches[0].translation );

				translate_api.log.push( translate_api.settings.text);

				var temp = translate_api.settings.fromLanguage;
				translate_api.settings.fromLanguage = translate_api.settings.toLanguage;
				translate_api.settings.toLanguage = temp;

				translate_api._triggerNext();
			});
		},

		_decode : function(html){
			return $('<span>').html(html).text();
		},

		_triggerNext : function(){
			if(translate_api.settings.currentBlockIndex >= translate_api.settings.loops * 2) {
				translate_api.settings.success();
				return;
			}

			translate_api.settings.currentBlockIndex++;
			translate_api._trans();
		}
	};

	return translate_api;
}();

var  tutorial = function(){
	var api = {
		startTutorial : function(){
			$('form *, #tutorial, #twitter').css('display', 'block');
			$('#content').height( $('form').height() + 80 ) ;
			$('#lower').height($(document).height() - $('#lower').offset().top);

			api.animateTutWindowTo(api.steps[api.settings.stepNo].target, 0, function(){
				api.showText(api.steps[api.settings.stepNo].target, api.steps[api.settings.stepNo].message, 0, function(){
					api.hideText(api.settings.duration, function(){
						api.settings.stepNo += 1;
						api.doStep();
					});
				});
			});

		},
		settings : {
			duration : 4500,
			stepNo : 0
		},
		doStep : function(){
			api.animateTutWindowTo(api.steps[api.settings.stepNo].target, api.settings.duration / 3, function(){
				api.showText(api.steps[api.settings.stepNo].target, api.steps[api.settings.stepNo].message, api.settings.duration / 4, function(){
					api.hideText(api.settings.duration, function(){
						api.callback();
					});
				});
			});
		},
		callback : function(){
			api.settings.duration = 4500;
			api.settings.stepNo += 1;
			if(api.settings.stepNo + 1 <= api.steps.length){
				api.doStep();
			}else{
				api.finishTutorial(api.settings.duration);
			}
		},
		finishTutorial : function(duration){
			setTimeout(function(){
				$('#tutorial, #twitter').css('display','none');
				$('#text-box').trigger('focus');
				api.settings.stepNo = 0;
			}, duration);
		},
	
		animateTutWindowTo : function(selector, duration, callback){
			setTimeout(function(){

				var element = $(selector),
					elOffset = element.offset(),
					elHeight = element.height(),
					elWidth = element.width();

				$('#dark-left').css({'width' : elOffset.left});
				$('#dark-right').css({'width' : $(document).width() - elOffset.left - elWidth});

				$('#upper').css({height : elOffset.top});
				$('#middle').css({height : elHeight});
				$('#lower').css({height : $(document).height() - elOffset.top - elHeight});
				$('#highlight').css({height : elHeight, width : elWidth});

				callback();
			}, duration);
		},
		showText : function(element, msg, duration, callback){
			setTimeout(function(){
				
				$('#message')
				.text(msg)
				.css({
					opacity : 1
				});

				var msgOffset = $('#message').height() +  parseInt( $('#message').css('margin-bottom') );

				$.scrollTo(element, duration,{
					offset : -msgOffset
				});

				callback();
			}, duration);
		},
		hideText : function(duration, callback){
			setTimeout(function(){

				$('#message').css({
					opacity : 0
				});

				callback();
			},duration);
		},
		steps : [
			{
				message : "Write some text here in order to get it translated into another language and back again.",
				target : '#text-box',
				duration : ""
			},
			{
				message : "Expect your result in this box! Altough don't expect it to be exactly the same... ^^",
				target : '#translation',
				duration : ""
			},
			{
				message : "Here is the log, where you can see the steps between the initial phrase and the final one.",
				target : '#log-box',
				duration : ""
			},
			{
				message : "Here you can choose your language and the language you want to translate to and back again.You are not allowed to have the same language in both boxes.",
				target : '#languages',
				duration : ""
			},
			{
				message : "This is the slider. Basically, it let's you decide how many cycle translations you want to make. Slide it, it gets happy! :)",
				target : '#slider',
				duration : ""
			},
			{
				message : "Press the 'Go!' button...",
				target : '#go-btn',
				duration : ""
			},
			
		]
	};
	return api;
}();

$(document).ready(function(){

	var width = $('#content').width();
	
	var img = '';
	var s = new SmileySlider(document.getElementById("slider"), img, width);
	var loops, from, to, message;

	$('.submit-btn').addClass('invalid');
	$('#go-btn').off('click');

	$('#content').height( $('form').height() + 80);

    s.position(0); // make it sad
    s.position(1); // make it happy
    
    var p = s.position(); // get it's position
    s.position(p / 2); // make it half as happy
    
    s.position(function (p) {
        // do something when it changes
        var min = 1;
        var max = 2;
        var dif = max - min;

        loops = Math.floor( p * dif + min );
        $('#cycle').text('How many times: ' + loops );
    });

	$('#loader').css({'display' : 'none'});   

	$('#content').on('change', function(){

	    from = $('.from-language').eq(1).val().toLowerCase();
	    to = $('.to-language').eq(1).val().toLowerCase();
	    message = $.trim( $('#text-box').val().toString() );
	    $('#log-box').val('');

		if(from != to && message != ""){
			$('#go-btn').removeAttr('disabled');
			$('.submit-btn').removeClass('invalid');
		}else{
			$('#go-btn').attr('disabled', 'disabled');
			$('.submit-btn').addClass('invalid');
		}
	});

    $('form').on('submit', function(e){
    	e.preventDefault();
		$('#log-box').val('');
	    translate.translate({
	    	text : message,
	    	loops : loops,
	    	fromLanguage : from,
	    	toLanguage : to,
	    	success : function(){
	    		$('#trans-label').css('display','block');
	    		$('#translation').val(translate.settings.text).css('display','block');

	    		$.each(translate.log, function(i, currentLog){
		    		$('#log-box').val( $('#log-box').val() + translate.log[0] + "\n" );
	    		});

				translate.log.length = 0;

	    		$('#log-label').css('display','block');
	    		$('#log-box').css('display','block');

	    		$('#loader').css('display','none');
	    		$('#content').height( $('form').height() + 80 );

			 //   $('#twitter').css({'display':'block'});

			   // var share = "I've translated this: \"" + $('#text-box').val() + "\"\nInto this: \"" + $('#translation').val() + "\" ";
			   // $('#twitter a').attr("href", "http://twitter.com/intent/tweet?url=" + encodeURIComponent("https://www.google.com") +"&text=" + encodeURIComponent(share));
	    	}
	    });
    });

    $('#text-box').on('focus', function(){
    	$('#trans-label').css('display', 'none');
    	$('#translation').css('display', 'none');

		$('#log-label').css('display','none');
		$('#log-box').css('display','none').val('');

		$('#content').height( $('form').height() + 80 ) ;
    });

    $(window).resize( function(){
		$('#slider').empty();
		width = $('#content').width();
		s = new SmileySlider(document.getElementById("slider"), img, width);

    	s.position(function (p) {
        // do something when it changes
        	var min = 1;
        	var max = 2;
        	var dif = max - min;
	
	        	loops = Math.floor( p * dif + min );
        	$('#cycle').text('How many times: ' + loops );
    	});

    	$('#content').height( $('form').height() + 80 ) ;
    	$('#lower').height($(document).height() - $('#lower').offset().top);
	});

	$('#tutorial-btn').on('click', function(e){
		e.preventDefault();
		tutorial.startTutorial();
	});
});



