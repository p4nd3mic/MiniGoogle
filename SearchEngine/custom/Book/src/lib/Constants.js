(function(lib){
	var constants = lib.util.extendNamespace('constants');

	constants.Condition = {'NEW' : 0, 'USED' : 1, 'EBOOK' : 2, 'RENTAL' : 3};
	constants.domPrefix = "bs-";
	
	constants.scrollBar = {
		minHandleSize : 50,
		mouseWheelSpeed : 50
	}

	constants.css = {
		
		twoColumnView : 'two_column_view',
		twoColumnViewColumn : 'two_column_view_column',
		twoColumnViewShifter : 'two_column_view_shifter',
		twoColumnViewSwitchButton : 'two_column_view_switch_button',
		selected : 'selected',
		imageBox : 'image_box',
		bookImage : 'book_img', 
		bookSizeSmall : 'book_img_small',
		bookSizeLarge : 'book_img_large',
		list : 'list',
		listBooks : 'list_books',
		emptyIndicator : 'empty_indicator',
		empty : 'empty',
		loading : 'loading',
		listItem : 'list_item',
		listItemFade : 'list_item_fade',
		listTextDark : 'list_text_dark',
		listTextMedium : 'list_text_medium',
		listTextLight : 'list_text_light',
		listBookItem : 'list_book_item', 
		listBookItemTitle : 'list_book_item_title',
		listBookItemMetaText : 'list_book_item_meta_text',
		listLoadMore : 'list_load_more',
		listOfferItem : 'list_offer_item',
		listOfferItemImage : 'list_offer_item_image',
		listOfferItemPrice : 'list_offer_item_price',
		listOfferItemShipping : 'list_offer_item_shipping',
		tabView : 'tab_view',
		tabWell : 'tab_well',
		tabContentContainer : 'tab_content_container',
		tabButton : 'tab_button',
		tabContent : 'tab_content',
		bookDetails : 'book_details',
		bookDetailsImageWell : 'book_details_image_well',
		bookDetailsData : 'book_details_data',
		bookDetailsTitle : 'book_details_title',
		bookDetailsRow : 'book_details_row',
		bookDetailsLabel : 'book_details_label',
		bookDetailsText : 'book_details_text',
		scrollBarParent : 'scroll_bar_parent',
		scrollBarGutter : 'scroll_bar_gutter',
		scrollBarHandle : 'scroll_bar_handle',
		keywordInput : 'keyword_input',
		keywordInputForm : 'keyword_input_form',
		keywordInputTextField : 'keyword_input_text_field',
		keywordInputTextFieldWrapper : 'keyword_input_text_field_wrapper',
		keywordInputSubmit : 'keyword_input_submit'
	};

	constants.strings = {
		bookMetaNames : {
			author : 'Author',
			isbn10 : 'ISBN10',
			isbn13 : 'ISBN13',
			binding : 'Binding',
			msrp : 'MSRP',
			pages : 'Pages',
			publisher : 'Publisher',
			publishedDate : 'Published',
			edition : 'Edition'
		},
		listBookLabels : {
			loadMore : 'Load More',
			emptyList : 'No Books to Display'
		},
		tabLabels : {
			empty : 'No Book to Display',
			pricesEmpty : 'No Prices Found'
		},
		listOffersLabels : {
			emptyList : 'No Prices Found',
			totalPrice : 'Total Price',
			shipping : 'shipping'
		},
		keywordInput : {
			defaultText : 'Title, Author, ISBN, Keywords',
			submit : 'Search'
		}
	};
	
	constants.resources = {
		images : {
			ajaxLoaderOnWhite : 'img/loader-on-white.gif'
		}
	};
})(JSBookSearch);
