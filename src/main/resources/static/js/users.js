window.authReady = (async () => {
  const storedToken = sessionStorage.getItem("iotBearerToken");
  const headers = storedToken ? { Authorization: `Bearer ${storedToken}` } : {};
  const response = await fetch("/api/auth/me", { headers });
  if (!response.ok) throw new Error("Sessione non valida");
  return response.json();
})()
  .then((user) => {
    if (!user) return null;
    sessionStorage.setItem("iotBearerToken", user.accessToken);
    if (user.role !== "ADMIN") {
      window.location.replace("/index.html");
      return null;
    }
    document.body.classList.add("auth-ready");
    return user;
  })
  .catch(() => {
    sessionStorage.removeItem("iotBearerToken");
    window.location.replace("/login.html");
    return null;
  });

const userList = document.querySelector("#user-list");
const userCount = document.querySelector("#user-count");
const editor = document.querySelector("#editor");
const userForm = document.querySelector("#user-form");
const userIdInput = document.querySelector("#user-id");
const passwordInput = document.querySelector("#password");
const formTitle = document.querySelector("#form-title");
const saveButton = document.querySelector("#save-user");
const formMessage = document.querySelector("#form-message");

function authFetch(url, options = {}) {
  const token = sessionStorage.getItem("iotBearerToken");
  if (!token) {
    window.location.replace("/login.html");
    return Promise.reject(new Error("Sessione terminata"));
  }

  const headers = new Headers(options.headers ?? {});
  headers.set("Authorization", `Bearer ${token}`);
  return fetch(url, { ...options, headers }).then((response) => {
    if (response.status === 401) {
      sessionStorage.removeItem("iotBearerToken");
      window.location.replace("/login.html");
    }
    return response;
  });
}

function showFormMessage(text, isError = false) {
  formMessage.textContent = text;
  formMessage.classList.toggle("error", isError);
}

function resetForm() {
  userForm.reset();
  userIdInput.value = "";
  passwordInput.required = true;
  document.querySelector("#password-hint").textContent =
    "obbligatoria per un nuovo utente";
  formTitle.textContent = "Nuovo utente";
  saveButton.textContent = "Crea utente";
  showFormMessage("");
  editor.hidden = true;
}

function createUserRow(user, index) {
  const row = document.createElement("li");
  row.className = "user-row";
  row.style.animationDelay = `${Math.min(index * 25, 250)}ms`;

  const details = document.createElement("div");
  const primary = document.createElement("div");
  primary.className = "user-primary";
  const name = document.createElement("span");
  name.className = "user-name";
  name.textContent = `${user.firstname} ${user.lastname}`;
  const username = document.createElement("span");
  username.className = "user-username";
  username.textContent = `@${user.username}`;
  const role = document.createElement("span");
  role.className = "role-badge";
  role.textContent = user.role;
  primary.append(name, username, role);

  const secondary = document.createElement("div");
  secondary.className = "user-details";
  if (user.email) {
    const email = document.createElement("span");
    email.className = "user-email";
    email.textContent = user.email;
    secondary.append(email);
  }
  if (user.tUsercol) {
    const extra = document.createElement("span");
    extra.className = "user-extra";
    extra.textContent = user.tUsercol;
    secondary.append(extra);
  }
  details.append(primary, secondary);

  const actions = document.createElement("div");
  actions.className = "user-actions";
  const editButton = document.createElement("button");
  editButton.className = "secondary small";
  editButton.type = "button";
  editButton.textContent = "Modifica";
  editButton.dataset.action = "edit";
  editButton.dataset.id = user.id;
  const deleteButton = document.createElement("button");
  deleteButton.className = "danger small";
  deleteButton.type = "button";
  deleteButton.textContent = "Elimina";
  deleteButton.dataset.action = "delete";
  deleteButton.dataset.id = user.id;
  actions.append(editButton, deleteButton);

  row.append(details, actions);
  return row;
}

async function loadUsers() {
  userList.setAttribute("aria-busy", "true");
  userCount.textContent = "Caricamento...";
  try {
    const response = await authFetch("/api/users", {
      headers: { Accept: "application/json" },
    });
    if (!response.ok) throw new Error(`Errore ${response.status}`);
    const users = await response.json();
    userList.replaceChildren();
    if (!Array.isArray(users) || users.length === 0) {
      const empty = document.createElement("li");
      empty.className = "notice";
      empty.textContent = "Nessun utente trovato.";
      userList.append(empty);
    } else {
      users.forEach((user, index) =>
        userList.append(createUserRow(user, index)),
      );
    }
    userCount.textContent = `${users.length} ${users.length === 1 ? "account" : "account"}`;
  } catch (error) {
    userList.replaceChildren();
    const failure = document.createElement("li");
    failure.className = "notice form-message error";
    failure.textContent = `Impossibile caricare gli utenti. ${error.message}`;
    userList.append(failure);
    userCount.textContent = "Errore";
  } finally {
    userList.setAttribute("aria-busy", "false");
  }
}

function editUser(user) {
  userIdInput.value = user.id;
  userForm.elements.firstname.value = user.firstname ?? "";
  userForm.elements.lastname.value = user.lastname ?? "";
  userForm.elements.username.value = user.username ?? "";
  userForm.elements.email.value = user.email ?? "";
  userForm.elements.role.value =
    user.role.toLowerCase() === "administrator"
      ? "admin"
      : user.role.toLowerCase();
  userForm.elements.tUsercol.value = user.tUsercol ?? "";
  passwordInput.value = "";
  passwordInput.required = false;
  document.querySelector("#password-hint").textContent =
    "lascia vuota per non modificarla";
  formTitle.textContent = `Modifica utente #${user.id}`;
  saveButton.textContent = "Salva modifiche";
  showFormMessage("");
  editor.hidden = false;
  editor.scrollIntoView({ behavior: "smooth", block: "start" });
  userForm.elements.firstname.focus();
}

userForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const id = userIdInput.value;
  const payload = {
    firstname: userForm.elements.firstname.value.trim(),
    lastname: userForm.elements.lastname.value.trim(),
    username: userForm.elements.username.value.trim(),
    email: userForm.elements.email.value.trim() || null,
    role: userForm.elements.role.value,
    tUsercol: userForm.elements.tUsercol.value.trim() || null,
  };
  if (passwordInput.value) payload.password = passwordInput.value;

  saveButton.disabled = true;
  showFormMessage(id ? "Salvataggio modifiche..." : "Creazione utente...");
  try {
    const response = await authFetch(id ? `/api/users/${id}` : "/api/users", {
      method: id ? "PUT" : "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const detail = await response.text();
      throw new Error(detail || `Errore ${response.status}`);
    }
    resetForm();
    await loadUsers();
  } catch (error) {
    showFormMessage(`Salvataggio non riuscito. ${error.message}`, true);
  } finally {
    saveButton.disabled = false;
  }
});

document.querySelector("#new-user").addEventListener("click", () => {
  resetForm();
  editor.hidden = false;
  editor.scrollIntoView({ behavior: "smooth", block: "start" });
  userForm.elements.firstname.focus();
});
document.querySelector("#cancel-edit").addEventListener("click", resetForm);

userList.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;
  const userId = Number(button.dataset.id);

  if (button.dataset.action === "edit") {
    try {
      const response = await authFetch(`/api/users/${userId}`, {
        headers: { Accept: "application/json" },
      });
      if (!response.ok) throw new Error(`Errore ${response.status}`);
      editUser(await response.json());
    } catch (error) {
      alert(`Impossibile leggere l'utente. ${error.message}`);
    }
    return;
  }

  if (button.dataset.action === "delete") {
    if (!window.confirm(`Eliminare l'utente #${userId}?`)) return;
    try {
      const response = await authFetch(`/api/users/${userId}`, {
        method: "DELETE",
      });
      if (!response.ok) throw new Error(`Errore ${response.status}`);
      if (userIdInput.value === String(userId)) resetForm();
      await loadUsers();
    } catch (error) {
      alert(`Eliminazione non riuscita. ${error.message}`);
    }
  }
});

document.querySelector("#logout").addEventListener("click", () => {
  authFetch("/api/auth/logout", { method: "POST" }).finally(() => {
    sessionStorage.removeItem("iotBearerToken");
    window.location.replace("/login.html");
  });
});

window.authReady.then((user) => {
  if (!user || user.role !== "ADMIN") return;
  document.querySelector("#current-user").textContent =
    `${user.username} · ${user.role}`;
  loadUsers();
});
