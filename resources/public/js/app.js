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
  document.getElementById('delete-account-btn').disabled = !button.checked;
}

function closeModal(button) {
  button.closest('dialog').close();
  document.getElementById('popup').remove();
}

function sortTable(n, th) {
  // https://www.w3schools.com/howto/howto_js_sort_table.asp
  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
  table = document.getElementById("transaction-table");

  // If there is only one row, do nothing
  if (table.rows.length <= 2) {
    return; 
  }

  switching = true;
  dir = th.getAttribute("data-sort") === "asc" ? "desc" : "asc";
  
  // Reset all headers' icons
  document.querySelectorAll(".sort-icon").forEach(icon => {
    icon.innerHTML = '';
  });

  while (switching) {
    switching = false;
    rows = table.rows;
    for (i = 1; i < (rows.length - 1); i++) {
      shouldSwitch = false;
      x = rows[i].getElementsByTagName("TD")[n];
      y = rows[i + 1].getElementsByTagName("TD")[n];

      if (dir === "asc") {
        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
          shouldSwitch = true;
          break;
        }
      } else if (dir === "desc") {
        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
          shouldSwitch = true;
          break;
        }
      }
    }
    if (shouldSwitch) {
      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
      switching = true;
      switchcount++;
    } else {
      if (switchcount === 0) {
        dir = dir === "asc" ? "desc" : "asc";
        switching = true;
      }
    }
  }
  
  // Update the clicked column header's icon
  th.querySelector(".sort-icon").innerHTML = `<img src="/icons/dropdown.svg" id="sort-btn-${dir}" alt="Sort ${dir}">`;
  th.setAttribute("data-sort", dir);
}


function toggleEditFields(button) {
  var inputs = document.querySelectorAll('input');
  var selects = document.querySelectorAll('select');
  var textarea = document.querySelectorAll('textarea');
  var modifyBtn = document.getElementById('modify-transaction-btn');

  // Toggle all input fields
  inputs.forEach(function(input) {
    input.disabled = !input.disabled;
  });
  selects.forEach(function(select) {
    select.disabled = !select.disabled;
  });
  textarea.forEach(function(textar) {
    textar.disabled = !textar.disabled;
  })

  // Toggle Modify transaction button
  modifyBtn.disabled = !modifyBtn.disabled;

  // make sure the toggle button itself is not disabled 
  // as it is also an input type
  button.disabled = false; 
}