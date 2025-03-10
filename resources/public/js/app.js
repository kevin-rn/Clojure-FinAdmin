const toggleButton = document.getElementById('toggle-btn')
const sidebar = document.getElementById('sidebar')

$(document).ready(function () {
    $("#sidebar a").on("click", function () {
      $("#sidebar").find(".selected").removeClass("selected");
      $(this).parent().addClass("selected");
    });
  });

function toggleSidebar() {
    sidebar.classList.toggle('close')
    toggleButton.classList.toggle('rotate')
    clossAllSubMenus()
}

function toggleSubMenu(button) {

    if(!button.nextElementSibling.classList.contains('show')) {
        clossAllSubMenus()
    }

    button.nextElementSibling.classList.toggle('show')
    button.classList.toggle('rotate')

    if(sidebar.classList.contains('close')) {
        sidebar.classList.toggle('close')
        toggleButton.classList.toggle('rotate')
    }
}

function clossAllSubMenus(){
    Array.from(sidebar.getElementsByClassName('show')).forEach(ul => {
        ul.classList.remove('show')
        ul.previousElementSibling.classList.remove('rotate')
    })
}