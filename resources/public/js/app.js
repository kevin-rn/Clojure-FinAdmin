const toggleButton = document.getElementById('toggle-btn');
const sidebar = document.getElementById('sidebar');

$(document).ready(function () {
  // Highlight selected menu item when clicked.
  $('#sidebar a').on('click', function () {
    $('#sidebar').find('.selected').removeClass('selected');
    $(this).parent().addClass('selected');
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
  const parentDiv = button.closest('.input-group')
  const input = parentDiv.querySelector('input.password');
  const visibilityIcon = parentDiv.querySelector('label.visibility-icon');

  if (input.hasAttribute('type')) {
      if (input.type === 'password') {
          input.type = 'text';
          visibilityIcon.classList.add('visible');
          visibilityIcon.classList.remove('hidden');
      } else {
          input.type = 'password';
          visibilityIcon.classList.add('hidden');
          visibilityIcon.classList.remove('visible');
      }
  }
}

function toggleAcknowledge(button) {
  document.getElementById('delete-account-btn').disabled = !button.checked
}


function closeModal(button) {
  button.closest('dialog').close();
  document.getElementById('popup').remove()
  
}