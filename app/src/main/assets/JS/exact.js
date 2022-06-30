$(document).ready(function(){

    $("input[type=email]").blur(function(){
        var mail = $(this).val();
        if(!validateEmail(mail)){
            alert(Android.getString('InvalidEmail'));
            $(this).focus();
        }

    });

    $("input[type=number]").blur(function(){
        var inp = parseInt($(this).val());
        if(typeof inp === "number"){
            if(inp > 0 || $(this).val() == ""){
                
            }else{
                alert(Android.getString('InvalidNumber'));
            }   
        }else{       
                alert(Android.getString('InvalidNumber'));
        } 
    });


//    if(sessionStorage.getItem("user") == "in"){
//        document.body.style.backgroundColor = "#3CB371";
//    }else{
//        document.body.style.backgroundColor = "#FFF";
//    }
    var $ctl = $('[strName]');
    $ctl.each(function(){
        var str = $(this).attr("strName");
        if ($(this).is("input"))
            $(this).val(Android.getString(str));
        else
            $(this).text(Android.getString(str));
     });

     setControls();



 });


    function bindDropdown(ctl, Source, Value, Text,SelectValue, SelectText){
       var $dataSource = $.parseJSON(Source);
       var $ddl = $('#' + ctl);

    //    if(ctl == "ddlCurrentRegion"){
    //        alert(ctl +"/"+ Source +"/"+ Value +"/"+ Text +"/"+ SelectValue +"/"+ SelectText);
    //    } 

        $ddl.empty();

        if ($dataSource.length == 0){
            return;
        }
            
       if(SelectText != null){
           $ddl.append($('<option></option>').val(SelectValue).text(SelectText));
       }
            
       $.each($dataSource, function(i, obj){
            var id = $dataSource[i][Value];
            var disp = $dataSource[i][Text];

            $ddl.append($('<option></option>').val(id).text(disp));

       });


    }

    function setControls(){
        var $ctls = $('li');
        $ctls.each(function(){
            var id = $(this).attr("id");
            if(id != null){
                var Adjustibility = Android.getControl(id);

                switch(Adjustibility){
                    case "N":
                        $(this).css("display", "none");
                        break;
                    case "O":
                        $(this).show();
                        break;
                    case "M":
                        var $control = $(this).find("input, select, textarea")
                        $control.prop("required", true);
                        break;


                }
            }

        })
    }

    function isFormValidated(){
        var $mandatory = $('[required]');
        var passed = true;
        $mandatory.each(function(){
            if($(this).val() == "" || $(this).val() == 0){//|| $(this).val() == 0
                // if(sessionStorage.getItem("FamilyData") == null){
                //     passed = false;
                // }
                passed = false;
            }
         });
         return passed;
    }

    function getControlsValuesJSON(Container){
        var $lis = $(Container).find("input, select, textarea");
        var array = [];
        $lis.each(function(){
            array.push({
                id: $(this).attr("id"),
                value: $(this).val()
            });
        });

        var jsonData = JSON.stringify(array);

        return jsonData;
    }


    function LoadList(Source, Container, Controls, Columns){
        var $dataSource = $.parseJSON(Source);
        var $ctl = $(Container);
        var ctls = Controls;

        var $li = $ctl.children().first();
        var liHTML = $li.html();

        //Remove the first empty li
        $li.remove();

        $.each($dataSource, function(key, value){
            $li = $("<li>").append(liHTML);

            $.each(ctls, function(index, val){
                var controlName = '#' + val;
                var $control = $li.find(controlName);
                var column = Columns[index];
                var value = $dataSource[key][column];

                if($control.is('input, select, textarea'))
                    $control.val(value);
                else
                    $control.text(value);
            });

            $ctl.append($li);

        });

    }

    function queryString(name){
        var url = "http://" + window.location.href.substr(window.location.href.lastIndexOf("/") + 1, window.location.href.length);
        return Android.queryString(url, name);
    }

    function bindDataFromDatafield(source){
        var dataSource = $.parseJSON(source);
        var ctls = $("[dataField]");

        $.each(ctls, function(){
            var key = $(this).attr("dataField");

            if($(this).is('input, select, textarea'))
                $(this).val(dataSource[0][''+ key +'']);
            else
                $(this).text(dataSource[0][''+ key +'']);
        });
    }

     function getDateForJS(date){
        var dDate = new Date(date);
        var d = dDate.getDate();
        var m = dDate.getMonth() + 1;
        var y = dDate.getFullYear();

        if (d < 10)
            d = '0' + d;
        if (m < 10)
            m = '0' + m;

        return y + '-' + m + '-' + d;
    }

    function noImage(img){
        $(img).attr('src', '../images/person.gif');
    }

    //********************************Context menu starts*************************************************
    var contextMenu = {
        menuDots:{
                    position:"absolute",
                    right:"0px",
                    top:"0"
                },
        clearFix:{
                    content:"",
                    display:"table",
                    clear:"both"
                },
        ulSideDropMenu:{
                    padding:"3px"
                },
        dropdownContent:{
                     //display:"none",
                     position:"absolute",
                     "background-color":"#f9f9f9",
                     padding:"2px",
                     overflow:"auto",
                     "box-shadow" : "0px 8px 16px 0px rgba(0,0,0,0.2)",
                     right:"0",
                     "z-index" :"100"
                 },
        contextLi:{
                    color:"black",
                    padding:"10px 10px",
                    "text-decoration":"none",
                    display:"block",
                    margin:"5px 0px"
                },

        assignStyle: function(){
            $(".dropdownContent").css(this.dropdownContent);
            $(".ulSideDropMenu").css(this.ulSideDropMenu);
            $(".contextLi").css(this.contextLi);
       },


        createContextMenu: function(menuList, callback){

            var $ctls = $('.dot-side-menu');

            $ctls.css({"position":"relative"});

            var containerHeight = $ctls.height();

            $("head").append($('<style>.menuDots:after{content:"";background-image:url("../images/vertical_dots.svg");background-size:100% 25px;background-position:50%;background-repeat:no-repeat;width:20px;height:'+ containerHeight +'px;float:right;}</style>'));

            var $divDots = $('<div class="menuDots">');
            $divDots.css(contextMenu.menuDots);
            $ctls.append($divDots);


            $(".menuDots").click(function(e){

                $("#mainDropDown").remove();

                var $divDropdown = $("<div id='mainDropDown'>");
                    $divDropdown.css({"position":"relative"});

                    var $ul = $('<ul class="ulSideDropMenu">');

                    var $List = menuList;
                    $.each($List, function(i, v){
                        $ul.append('<li id="ctx'+ v +'"  class="contextLi">'+ v +'</li>');
                    });

                    var $divContent = $('<div id="menuDrop" class="dropdownContent">');
                    var $divClearFix = $('<div class="clearFix">');

                    $divContent.append($ul);
                    $divDropdown.append($divContent);
                    $divDropdown.append($divClearFix);
                    $(this).append($divDropdown);

                    contextMenu.assignStyle();

//                    e.stopPropagation();



            });

            $(document).on("click", ".contextLi",function(){
                callback.call(this);
            });

            $(window).click(function(e){
                if($(e.target).attr("class") != 'menuDots')
                    $(".dropdownContent").hide();
            });
        }



    }

    function validateEmail(email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }
//********************************Context menu ends*************************************************