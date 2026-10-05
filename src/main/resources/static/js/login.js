const tokenKey = "iotBearerToken";
const loginForm = document.querySelector("#login-form");
const registerForm = document.querySelector("#register-form");
const loginTab = document.querySelector("#login-tab");
const registerTab = document.querySelector("#register-tab");
const title = document.querySelector("#title");
const message = document.querySelector("#message");

function setMode(mode) {
  const registering = mode === "register";
  loginForm.hidden = registering;
  registerForm.hidden = !registering;
  loginTab.setAttribute("aria-selected", String(!registering));
  registerTab.setAttribute("aria-selected", String(registering));
  title.textContent = registering ? "Crea il tuo account" : "Accedi";
  message.textContent = "";
  message.classList.remove("error");
}

function showMessage(text, isError = false) {
  message.textContent = text;
  message.classList.toggle("error", isError);
}

async function submitJson(url, payload) {
  return fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(payload),
  });
}

loginTab.addEventListener("click", () => setMode("login"));
registerTab.addEventListener("click", () => setMode("register"));

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const button = loginForm.querySelector('button[type="submit"]');
  button.disabled = true;
  showMessage("Verifica delle credenziali...");

  try {
    const response = await submitJson("/api/auth/login", {
      username: loginForm.elements.username.value.trim(),
      password: loginForm.elements.password.value,
    });
    if (!response.ok) {
      throw new Error(
        response.status === 401
          ? "Username o password non corretti."
          : `Accesso non riuscito (HTTP ${response.status}).`,
      );
    }

    const result = await response.json();
    sessionStorage.setItem(tokenKey, result.accessToken);
    window.location.replace("/index.html");
  } catch (error) {
    showMessage(error.message || "Impossibile contattare il server.", true);
  } finally {
    button.disabled = false;
  }
});

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const button = registerForm.querySelector('button[type="submit"]');
  button.disabled = true;
  showMessage("Creazione account...");

  try {
    const response = await submitJson("/api/auth/register", {
      firstname: registerForm.elements.firstname.value.trim(),
      lastname: registerForm.elements.lastname.value.trim(),
      username: registerForm.elements.username.value.trim(),
      email: registerForm.elements.email.value.trim() || null,
      password: registerForm.elements.password.value,
    });
    if (!response.ok) {
      throw new Error(
        response.status === 409
          ? "Questo username è già in uso."
          : `Registrazione non riuscita (HTTP ${response.status}).`,
      );
    }

    const username = registerForm.elements.username.value.trim();
    registerForm.reset();
    loginForm.elements.username.value = username;
    setMode("login");
    showMessage("Account creato con ruolo readonly. Ora puoi accedere.");
  } catch (error) {
    showMessage(error.message || "Impossibile contattare il server.", true);
  } finally {
    button.disabled = false;
  }
});

const existingToken = sessionStorage.getItem(tokenKey);
const sessionHeaders = existingToken
  ? { Authorization: `Bearer ${existingToken}` }
  : {};
fetch("/api/auth/me", { headers: sessionHeaders })
  .then(async (response) => {
    if (!response.ok) {
      sessionStorage.removeItem(tokenKey);
      return;
    }
    const user = await response.json();
    sessionStorage.setItem(tokenKey, user.accessToken);
    window.location.replace("/index.html");
  })
  .catch(() => sessionStorage.removeItem(tokenKey));
