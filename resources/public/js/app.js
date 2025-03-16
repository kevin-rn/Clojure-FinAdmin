const toggleButton = document.getElementById('toggle-btn');
const sidebar = document.getElementById('sidebar');

// jQuery to highlight selected menu item on click
$(document).ready(function () {
  $('#sidebar a').on('click', function () {
    $('#sidebar').find('.selected').removeClass('selected');
    $(this).parent().addClass('selected');
  });
});

/**
 * Toggles the sidebar's open/close state and rotates the toggle button.
 */
function toggleSidebar() {
  sidebar.classList.toggle('close');
  toggleButton.classList.toggle('rotate');
  closeAllSubMenus();
}

/**
 * Toggles the visibility of a submenu and rotates the corresponding button.
 * Closes all other submenus if any.
 * 
 * @param {HTMLElement} button - The button that triggered the submenu toggle.
 */
function toggleSubMenu(button) {
  const submenu = button.nextElementSibling;

  // Close other submenus if open
  if (!submenu.classList.contains('show')) {
    closeAllSubMenus();
  }

  submenu.classList.toggle('show');
  button.classList.toggle('rotate');

  // Automatically open sidebar if it's closed
  if (sidebar.classList.contains('close')) {
    sidebar.classList.remove('close');
    toggleButton.classList.remove('rotate');
  }
}

/**
 * Closes all open submenus in the sidebar.
 */
function closeAllSubMenus() {
  Array.from(sidebar.getElementsByClassName('show')).forEach(ul => {
    ul.classList.remove('show');
    ul.previousElementSibling.classList.remove('rotate');
  });
}

/**
 * Toggles the visibility of a password input field between text and password.
 * 
 * @param {HTMLElement} button - The button that triggered the visibility toggle.
 */
function togglePassword(button) {
  const parentDiv = button.closest('.input-group');
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

/**
 * Enables or disables the "Delete Account" button based on the checked state 
 * of a checkbox.
 * 
 * @param {HTMLElement} button - The checkbox element.
 */
function toggleAcknowledge(button) {
  document.getElementById('delete-account-btn').disabled = !button.checked;
}

/**
 * Closes the modal dialog and removes the popup element.
 * 
 * @param {HTMLElement} button - The button that triggered the modal close.
 */
function closeModal(button) {
  button.closest('dialog').close();
  document.getElementById('popup').remove();
}

/**
 * https://www.w3schools.com/howto/howto_js_sort_table.asp
 * Sorts a table based on the clicked header column.
 * 
 * @param {number} n - The column index to sort by.
 * @param {HTMLElement} th - The clicked table header element.
 */
function sortTable(n, th) {
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

/**
 * Enables or disables form input fields and the Modify transaction button.
 * 
 * @param {HTMLElement} button - The button that triggered the toggle.
 */
function toggleEditFields(button) {
  var inputs = document.querySelectorAll('input');
  var selects = document.querySelectorAll('select');
  var textarea = document.querySelectorAll('textarea');
  var modifyBtn = document.getElementById('modify-transaction-btn');

  // Toggle all input fields
  inputs.forEach(input => input.disabled = !input.disabled);
  selects.forEach(select => select.disabled = !select.disabled);
  textarea.forEach(textar => textar.disabled = !textar.disabled);

  // Toggle Modify transaction button
  modifyBtn.disabled = !modifyBtn.disabled;

  // Ensure the toggle button itself is not disabled
  button.disabled = false;
}

/**
 * Handles file input changes and displays selected file names in a list.
 * 
 * @param {Event} event - The input event triggered by file selection.
 */
function getFileData(event) {
  const files = event.target.files;
  const list = document.getElementById("document-files");

  if (files.length > 0) {
    list.innerHTML = "";

    for (let i = 0; i < files.length; i++) {
      const listItem = document.createElement("li");

      // Create the <p> element to hold the file name
      const textElement = document.createElement("p");
      textElement.textContent = files[i].name;

      // Create remove button
      const removeButton = document.createElement("button");
      removeButton.classList.add("remove-file");
      removeButton.textContent = "✖";
      removeButton.onclick = function () {
        listItem.remove();
      };

      // Append the <p> and button to the list item
      listItem.appendChild(textElement);
      listItem.appendChild(removeButton);

      // Append the list item to the list
      list.appendChild(listItem);
    }
  }
}

/**
 * Enables drag and drop functionality for the file input field.
 */
function dragAndDrop() {
  const dropzone = document.querySelector(".dropzone");
  const fileInput = document.getElementById("dropzone-file");

  dropzone.addEventListener("dragover", function (event) {
    event.preventDefault();
    dropzone.classList.add("drag-over");
  });

  dropzone.addEventListener("dragleave", function () {
    dropzone.classList.remove("drag-over");
  });

  dropzone.addEventListener("drop", function (event) {
    event.preventDefault();
    dropzone.classList.remove("drag-over");

    const files = event.dataTransfer.files;
    fileInput.files = files;
    getFileData({ target: fileInput });
  });

  dropzone.addEventListener("click", function () {
    fileInput.click();
  });
};
