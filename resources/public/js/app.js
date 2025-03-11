const toggleButton = document.getElementById('toggle-btn');
const sidebar = document.getElementById('sidebar');

$(document).ready(function () {
  // Highlight selected menu item when clicked
  $("#sidebar a").on("click", function () {
    $("#sidebar").find(".selected").removeClass("selected");
    $(this).parent().addClass("selected");
  });
});

// Toggle sidebar open/close
function toggleSidebar() {
  sidebar.classList.toggle('close');
  toggleButton.classList.toggle('rotate');
  closeAllSubMenus();
}

// Toggle submenu visibility
function toggleSubMenu(button) {
  const submenu = button.nextElementSibling;

  if (!submenu.classList.contains('show')) {
    closeAllSubMenus();
  }

  submenu.classList.toggle('show');
  button.classList.toggle('rotate');

  if (sidebar.classList.contains('close')) {
    sidebar.classList.remove('close');
    toggleButton.classList.remove('rotate');
  }
}

// Close all submenus
function closeAllSubMenus() {
  Array.from(sidebar.getElementsByClassName('show')).forEach(ul => {
    ul.classList.remove('show');
    ul.previousElementSibling.classList.remove('rotate');
  });
}

function togglePassword(button) {
    const input = button.closest('.input-group').querySelector('input[class="password"]');
    if (input.hasAttribute('type')) {
        if (input.type === 'password') {
          input.type = 'text'; // Change type to 'text' to show the password
        } else {
          input.type = 'password'; // Change type back to 'password' to hide the password
        }
      }
}
