/**
 * 
 */
 $(function()
{
    function setTab ( i )
    {
        selectTab(i);
    }
    
    function selectTab ( i )
    {
        if( i == 1){
            //case 1:
            get("fb_hint").style.display = "block";
            
            //break;
        } else if ( i == 2) {
            //case 2:
            get("fb_hint").style.display="none";
            //break;
            
        }
        
    }
    function get(id) {
        return document.getElementById(id);
    }
    function setupTab ( i )
    {
        selectSetupTab(i);
    }
    
    function selectSetupTab ( i )
    {
        if( i == 1){
            //case 1:
            get("syncAcc").style.display = "block";
            get("addAcc").style.display = "none";
            get("setPs").style.display = "none";
            get("tabA").className = "link_Act";
            get("tabB").className = "link_Normal";
            get("tabC").className = "link_Normal";
            //break;
        } else if ( i == 2) {
            //case 2:
            get("syncAcc").style.display = "none";
            get("addAcc").style.display = "block";
            get("setPs").style.display = "none";
            get("tabA").className = "link_Normal";
            get("tabB").className = "link_Act";
            get("tabC").className = "link_Normal";
            //break;
            
        }else if ( i == 3) {
            //case 2:
            get("syncAcc").style.display = "none";
            get("addAcc").style.display = "none";
            get("setPs").style.display = "block";
            get("tabA").className = "link_Normal";
            get("tabB").className = "link_Normal";
            get("tabC").className = "link_Act";
            //break;
            
        }
        
    }
    function get(id) {
        return document.getElementById(id);
    }
});