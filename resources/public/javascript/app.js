$(function(){
  $('#date').datepicker({dateFormat: 'yy-mm-dd'});
  $('#stats').tabs();

  $('.edit-date').editable(editstuff, { 
      type: 'datepicker',
      datepicker: {dateFormat: 'yy-mm-dd'}
    }
  );

  $('.edit-type').editable(editstuff, { 
      type: 'select',
      data: " {'beer':'beer','wine':'wine','liquor':'liquor','cocktail':'cocktail','everything':'everything'}",
      submit  : 'OK',
    }
  );

  $('.edit').editable(editstuff, { 
      submit  : 'OK'
    }
  );

  $('.delete').on('click', function(){
    var tr = $(this).parent();

    if (confirm('Delete this entry?')){
      $.ajax({
        type: "POST",
        cache: false,
        url: '/cost/delete',
        async: false,
        dataType: 'json',
        data: {'id': $(this).parent().attr('data-cost-id')},
        success: function(data){
          tr.hide();
        },
        error : function(req){result = "error";}
      }); 
    }
  
  })

});

function editstuff(value, settings){

    var that = this;
    var result;

    $.ajax({
      type: "POST",
      cache: false,
      url: '/cost/edit',
      async: false,
      dataType: 'json',
      data: {'id': $(this).parent().attr('data-cost-id'), 'value': value, 'field': $(this).attr('data-field')},
      success: function(data){
        result = data.value;
        $(that).attr('data-date', data.value);
        if ($(that).attr('class') === "edit-date") $(that).siblings().attr('data-date', data.value);
      },
      error : function(req){result = "error";}
    });
    
    return(result);

}
