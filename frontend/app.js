const app = document.getElementById('app');
const authLink = document.getElementById('auth-link');
let currentUser = null;
let authToken = null;
let adminEditingPropertyId = null;

const routes = {
  home: renderHome,
  search: renderSearch,
  annonce: renderAnnonce,
  login: renderLogin,
  signup: renderSignup,
  favorites: renderFavorites,
  admin: renderAdmin,
};

async function hashPassword(password) {
  const encoder = new TextEncoder();
  const data = encoder.encode(password);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(digest)).map((b) => b.toString(16).padStart(2, '0')).join('');
}

async function loginWithPassword(email, password) {
  const password_hash = await hashPassword(password);
  const response = await fetch('/api/auth', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password_hash }),
  });
  if (!response.ok) {
    throw new Error('Authentication failed');
  }
  const data = await response.json();
  authToken = data.token;
  currentUser = data.agent;
  updateNav();
  return currentUser;
}

function navigate() {
  const hash = window.location.hash.slice(1) || 'home';
  if (hash === 'admin' && !currentUser) {
    window.location.hash = 'login';
    return;
  }

  setActiveLink(hash);
  const renderer = routes[hash] || renderNotFound;
  renderer();
  app.focus();
}

function setActiveLink(hash) {
  document.querySelectorAll('.site-nav a').forEach((link) => {
    link.classList.toggle('active', link.getAttribute('href') === `#${hash}`);
  });
}

function renderHome() {
  app.innerHTML = `
    <section class="page-section hero" aria-labelledby="home-title">
      <div class="hero-grid">
        <div>
          <h1 id="home-title">Ymmo — Immobilier professionnel, rapide et clair</h1>
          <p>Découvrez des annonces de qualité, créez votre compte agent et gérez vos biens depuis une plateforme légère et accessible.</p>
          <div class="hero-actions">
            <a class="button button-primary" href="#search">Rechercher</a>
            <a class="button button-secondary" href="#signup">Créer un compte</a>
          </div>
        </div>
        <div class="card" style="background: linear-gradient(180deg, #eef2ff 0%, #ffffff 100%);">
          <h2>Valeur métier</h2>
          <p>Une interface fluide, une navigation claire, et des outils dédiés aux agents et aux administrateurs.</p>
        </div>
      </div>
    </section>
    <section class="page-section" aria-labelledby="services-title">
      <h2 id="services-title">Ce que propose Ymmo</h2>
      <div class="grid grid-3">
        <article class="card"><h3>Recherche intelligente</h3><p>Filtrez les annonces par type, statut, prix et localisation.</p></article>
        <article class="card"><h3>Espace agent</h3><p>Créez votre compte et gérez vos annonces depuis le tableau de bord agent.</p></article>
        <article class="card"><h3>Administration centralisée</h3><p>Les administrateurs visualisent toutes les données, les agents voient leurs propres biens.</p></article>
      </div>
    </section>
  `;
}

function renderSearch() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="search-title">
      <header class="panel-header">
        <div>
          <h2 id="search-title">Recherche de biens</h2>
          <p>Filtrez les annonces et consultez les résultats en direct.</p>
        </div>
        <a class="button button-secondary" href="#favorites">Voir mes favoris</a>
      </header>
      <form class="form-card" id="search-form" autocomplete="off">
        <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem;">
          <label>
            Type
            <select id="filter-type">
              <option value="">Tous</option>
              <option value="HOUSE">Maison</option>
              <option value="APARTMENT">Appartement</option>
              <option value="COMMERCIAL">Local commercial</option>
            </select>
          </label>
          <label>
            Statut
            <select id="filter-status">
              <option value="">Tous</option>
              <option value="AVAILABLE">Disponible</option>
              <option value="SOLD">Vendu</option>
              <option value="RENTED">Loué</option>
            </select>
          </label>
          <label>
            Prix max
            <input id="filter-price" type="number" inputmode="numeric" placeholder="250000" />
          </label>
          <label>
            Ville
            <input id="filter-city" type="text" placeholder="Paris" />
          </label>
        </div>
        <button type="submit" class="button button-primary">Lancer la recherche</button>
      </form>
    </section>
    <section class="page-section" aria-labelledby="results-title">
      <h2 id="results-title">Résultats</h2>
      <div id="search-results" class="listing-grid"></div>
    </section>
  `;

  document.getElementById('search-form').addEventListener('submit', (event) => {
    event.preventDefault();
    fetchSearchResults();
  });

  fetchSearchResults();
}

function fetchSearchResults() {
  const type = document.getElementById('filter-type').value;
  const status = document.getElementById('filter-status').value;
  const price = document.getElementById('filter-price').value;
  const city = document.getElementById('filter-city').value;

  const query = new URLSearchParams();
  if (type) query.append('type', type);
  if (status) query.append('status', status);
  if (price) query.append('price', price);
  if (city) query.append('city', city);

  fetch(`/api/properties?${query.toString()}`)
    .then((response) => response.ok ? response.json() : Promise.reject(response.status))
    .then(renderListings)
    .catch(() => renderSearchError());
}

function renderAnnonce() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="annonce-title">
      <h2 id="annonce-title">Annonce</h2>
      <div id="annonce-content"></div>
    </section>
  `;

  const propertyId = 1;
  fetch(`/api/properties/${propertyId}`)
    .then((response) => response.ok ? response.json() : Promise.reject())
    .then((property) => {
      document.getElementById('annonce-content').innerHTML = renderPropertyDetail(property);
    })
    .catch(() => {
      document.getElementById('annonce-content').innerHTML = '<div class="alert">Impossible de charger l\'annonce. Démarrez le backend ou vérifiez l\'identifiant.</div>';
    });
}

function renderPropertyDetail(property) {
  return `
    <article class="listing">
      <img src="https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80" alt="Photo du bien ${property.title}" />
      <div class="listing-body">
        <div>
          <h2>${property.title}</h2>
          <p class="listing-meta">${property.description}</p>
          <div class="chip">${property.type}</div>
        </div>
        <div>
          <p><strong>Prix :</strong> ${property.price} €</p>
          <p><strong>Surface :</strong> ${property.surface} m²</p>
          <p><strong>Pièces :</strong> ${property.rooms}</p>
          <p><strong>Statut :</strong> ${property.status}</p>
          <button class="button button-primary">Ajouter aux favoris</button>
        </div>
      </div>
    </article>
  `;
}

function renderLogin() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="login-title">
      <h2 id="login-title">Connexion</h2>
      <form class="form-card" id="login-form" autocomplete="username">
        <div class="field-group">
          <label for="login-email">Adresse e-mail</label>
          <input id="login-email" name="email" type="email" required placeholder="hello@ymmo.com" />
        </div>
        <div class="field-group">
          <label for="login-password">Mot de passe</label>
          <input id="login-password" name="password" type="password" required placeholder="••••••••" />
        </div>
        <button class="button button-primary" type="submit">Se connecter</button>
        <p id="login-message" class="listing-meta"></p>
        <p class="listing-meta">Pas encore de compte ? <a href="#signup">Créer un compte</a></p>
      </form>
    </section>
  `;

  document.getElementById('login-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    if (!email || !password) {
      document.getElementById('login-message').textContent = 'Veuillez saisir votre e-mail et votre mot de passe.';
      return;
    }

    try {
      await loginWithPassword(email, password);
      window.location.hash = 'admin';
    } catch (error) {
      document.getElementById('login-message').textContent = 'Identifiants incorrects.';
    }
  });
}

function renderSignup() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="signup-title">
      <h2 id="signup-title">Créer un compte agent</h2>
      <form class="form-card" id="signup-form" autocomplete="off">
        <div class="field-group">
          <label for="signup-name">Nom</label>
          <input id="signup-name" name="name" type="text" required placeholder="Julie Martin" />
        </div>
        <div class="field-group">
          <label for="signup-email">Adresse e-mail</label>
          <input id="signup-email" name="email" type="email" required placeholder="agent@ymmo.com" />
        </div>
        <div class="field-group">
          <label for="signup-phone">Téléphone</label>
          <input id="signup-phone" name="phone" type="tel" required placeholder="06 12 34 56 78" />
        </div>
        <div class="field-group">
          <label for="signup-password">Mot de passe</label>
          <input id="signup-password" name="password" type="password" required placeholder="••••••••" />
        </div>
        <button class="button button-primary" type="submit">Créer mon compte</button>
        <p id="signup-message" class="listing-meta"></p>
      </form>
    </section>
  `;

  document.getElementById('signup-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const name = document.getElementById('signup-name').value.trim();
    const email = document.getElementById('signup-email').value.trim();
    const phone = document.getElementById('signup-phone').value.trim();
    const password = document.getElementById('signup-password').value;

    if (!name || !email || !phone || !password) {
      document.getElementById('signup-message').textContent = 'Tous les champs sont obligatoires.';
      return;
    }

    try {
      const password_hash = await hashPassword(password);
      const payload = {
        name,
        email,
        phone,
        password_hash,
        is_admin: 'false',
        created_at: new Date().toISOString().slice(0, 10),
      };

      const response = await fetch('/api/agents', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!response.ok) {
        throw new Error('Signup failed');
      }
      await loginWithPassword(email, password);
      window.location.hash = 'admin';
    } catch (error) {
      document.getElementById('signup-message').textContent = 'Erreur lors de la création du compte. Vérifiez le backend.';
    }
  });
}

function renderFavorites() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="favorites-title">
      <h2 id="favorites-title">Favoris</h2>
      <p>Vos annonces sauvegardées apparaîtront ici.</p>
      <div id="favorites-list"></div>
    </section>
  `;

  if (!currentUser) {
    document.getElementById('favorites-list').innerHTML = '<div class="alert">Connectez-vous pour voir vos favoris.</div>';
    return;
  }

  document.getElementById('favorites-list').innerHTML = '<div class="card"><p>Aucune annonce favorite pour le moment.</p></div>';
}

function renderAdmin() {
  if (!currentUser) {
    app.innerHTML = `
      <section class="page-section" aria-labelledby="admin-login-title">
        <h2 id="admin-login-title">Espace agent</h2>
        <p class="listing-meta">Vous devez être connecté avec un compte agent pour accéder à cet espace.</p>
        <a class="button button-primary" href="#login">Se connecter</a>
      </section>
    `;
    return;
  }

  const title = currentUser.is_admin ? 'Panneau administrateur' : 'Espace agent';
  const summary = currentUser.is_admin
    ? 'Accédez à toutes les données et gérez l’ensemble des annonces.'
    : 'Gérez vos annonces et modifiez uniquement celles que vous avez créées.';

  app.innerHTML = `
    <section class="page-section">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">${title}</h2>
          <p class="listing-meta">${summary}</p>
        </div>
        <div class="panel-actions">
          <span class="badge">Agent : ${currentUser.name}</span>
          <span class="badge">${currentUser.is_admin ? 'Admin' : 'Utilisateur'}</span>
        </div>
      </div>
      <div class="grid admin-grid">
        <div class="form-card admin-card">
          <h3>Créer ou modifier une annonce</h3>
          <form id="admin-create-form" class="field-group">
            <label>
              Titre
              <input id="admin-title-field" type="text" placeholder="Maison de village" required />
            </label>
            <label>
              Description
              <textarea id="admin-description-field" placeholder="Description du bien" required></textarea>
            </label>
            <label>
              Prix
              <input id="admin-price-field" type="number" placeholder="230000" required />
            </label>
            <label>
              Surface m²
              <input id="admin-surface-field" type="number" placeholder="120" required />
            </label>
            <label>
              Pièces
              <input id="admin-rooms-field" type="number" placeholder="4" required />
            </label>
            <label>
              Type
              <select id="admin-type-field">
                <option value="HOUSE">Maison</option>
                <option value="APARTMENT">Appartement</option>
                <option value="COMMERCIAL">Local commercial</option>
              </select>
            </label>
            <label>
              Statut
              <select id="admin-status-field">
                <option value="AVAILABLE">Disponible</option>
                <option value="SOLD">Vendu</option>
                <option value="RENTED">Loué</option>
              </select>
            </label>
            <label>
              ID d'adresse
              <input id="admin-address-field" type="number" placeholder="1" required />
            </label>
            <div class="form-actions">
              <button type="submit" class="button button-primary">Enregistrer</button>
              <button type="button" class="button button-secondary" id="admin-reset">Réinitialiser</button>
            </div>
            <p id="admin-message" class="listing-meta"></p>
          </form>
        </div>
      </div>
    </section>
    <section class="page-section" aria-labelledby="my-properties-title">
      <h2 id="my-properties-title">${currentUser.is_admin ? 'Toutes les annonces' : 'Mes annonces'}</h2>
      <div id="admin-property-list" class="listing-grid"></div>
    </section>
  `;

  document.getElementById('admin-create-form').addEventListener('submit', handleAdminSubmit);
  document.getElementById('admin-reset').addEventListener('click', resetAdminForm);
  fetchAdminProperties();
}

function buildPropertyCard(property) {
  const owner = property.agent_id === currentUser.id;
  const canEdit = currentUser.is_admin || owner;

  return `
    <article class="listing property-card">
      <img src="https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80" alt="Photo du bien ${property.title}" />
      <div class="listing-body property-summary">
        <div>
          <h3>${property.title}</h3>
          <p class="listing-meta">${property.description}</p>
          <span class="status-pill">${property.status}</span>
        </div>
        <div>
          <p><strong>Prix :</strong> ${property.price} €</p>
          <p><strong>Surface :</strong> ${property.surface} m²</p>
          <p class="listing-meta">Agent #${property.agent_id}</p>
          <div class="property-actions">
            ${canEdit ? `<button class="button button-secondary" data-edit-id="${property.id}">Modifier</button>` : ''}
          </div>
        </div>
      </div>
    </article>
  `;
}

function fetchAdminProperties() {
  fetch('/api/properties', {
    headers: {
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
    },
  })
    .then((response) => response.ok ? response.json() : Promise.reject())
    .then((properties) => {
      const list = currentUser.is_admin
        ? properties
        : properties.filter((property) => property.agent_id === currentUser.id);
      document.getElementById('admin-property-list').innerHTML = list.length
        ? list.map(buildPropertyCard).join('')
        : '<div class="alert">Aucune annonce disponible.</div>';
      document.querySelectorAll('[data-edit-id]').forEach((button) => {
        button.addEventListener('click', () => openEditProperty(Number(button.dataset.editId), properties));
      });
    })
    .catch(() => {
      document.getElementById('admin-property-list').innerHTML = '<div class="alert">Impossible de charger les annonces. Vérifiez le backend.</div>';
    });
}

function openEditProperty(id, properties) {
  const property = properties.find((item) => item.id === id);
  if (!property) return;
  if (!currentUser.is_admin && property.agent_id !== currentUser.id) return;

  adminEditingPropertyId = id;
  document.getElementById('admin-title-field').value = property.title;
  document.getElementById('admin-description-field').value = property.description;
  document.getElementById('admin-price-field').value = property.price;
  document.getElementById('admin-surface-field').value = property.surface;
  document.getElementById('admin-rooms-field').value = property.rooms;
  document.getElementById('admin-type-field').value = property.type;
  document.getElementById('admin-status-field').value = property.status;
  document.getElementById('admin-address-field').value = property.address_id || 1;
  document.getElementById('admin-message').textContent = 'Modification en cours. Validez pour mettre à jour l\'annonce.';
}

function resetAdminForm() {
  adminEditingPropertyId = null;
  document.getElementById('admin-create-form').reset();
  document.getElementById('admin-message').textContent = '';
}

function handleAdminSubmit(event) {
  event.preventDefault();
  const title = document.getElementById('admin-title-field').value.trim();
  const description = document.getElementById('admin-description-field').value.trim();
  const price = Number(document.getElementById('admin-price-field').value);
  const surface = Number(document.getElementById('admin-surface-field').value);
  const rooms = Number(document.getElementById('admin-rooms-field').value);
  const type = document.getElementById('admin-type-field').value;
  const status = document.getElementById('admin-status-field').value;
  const address_id = Number(document.getElementById('admin-address-field').value);

  if (!title || !description || !price || !surface || !rooms || !type || !status || !address_id) {
    document.getElementById('admin-message').textContent = 'Tous les champs sont obligatoires pour créer ou modifier une annonce.';
    return;
  }

  const payload = {
    title,
    description,
    price,
    surface,
    rooms,
    type,
    status,
    agent_id: currentUser.id,
    address_id,
    created_at: new Date().toISOString().slice(0, 10),
  };

  const method = adminEditingPropertyId ? 'PUT' : 'POST';
  const url = adminEditingPropertyId ? `/api/properties/${adminEditingPropertyId}` : '/api/properties';

  fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
    },
    body: JSON.stringify(payload),
  })
    .then((response) => response.ok ? response.json() : Promise.reject())
    .then(() => {
      document.getElementById('admin-message').textContent = adminEditingPropertyId
        ? 'Annonce mise à jour.'
        : 'Annonce créée avec succès.';
      resetAdminForm();
      fetchAdminProperties();
    })
    .catch(() => {
      document.getElementById('admin-message').textContent = 'Erreur lors de l\'enregistrement. Vérifiez le backend ou les informations.';
    });
}

function renderNotFound() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="notfound-title">
      <h2 id="notfound-title">Page non trouvée</h2>
      <p>La page demandée n'existe pas. Utilisez la navigation en haut pour revenir à l'accueil.</p>
      <a class="button button-secondary" href="#home">Retour à l'accueil</a>
    </section>
  `;
}

function renderListings(properties) {
  const container = document.getElementById('search-results');
  if (!Array.isArray(properties) || properties.length === 0) {
    container.innerHTML = '<div class="alert">Aucun bien trouvé. Ajustez les filtres ou vérifiez le backend.</div>';
    return;
  }

  container.innerHTML = properties.map((property) => `
    <article class="listing" tabindex="0">
      <img src="https://images.unsplash.com/photo-1560185127-6f8b459c7241?auto=format&fit=crop&w=1200&q=80" alt="Photo du bien ${property.title}" />
      <div class="listing-body">
        <div>
          <h3>${property.title}</h3>
          <p class="listing-meta">${property.description}</p>
          <span class="chip">${property.type}</span>
        </div>
        <div>
          <p><strong>Prix :</strong> ${property.price} €</p>
          <p><strong>Surface :</strong> ${property.surface} m²</p>
          <a class="button button-secondary" href="#annonce">Voir</a>
        </div>
      </div>
    </article>
  `).join('');
}

function renderSearchError() {
  document.getElementById('search-results').innerHTML = '<div class="alert">Impossible de charger les annonces. Vérifiez que le backend fonctionne.</div>';
}

function updateNav() {
  const navAdmin = document.getElementById('nav-admin');
  const signupLink = document.getElementById('signup-link');
  if (!navAdmin || !signupLink) return;

  if (currentUser) {
    authLink.textContent = 'Déconnexion';
    authLink.href = '#home';
    signupLink.style.display = 'none';
    navAdmin.style.display = 'inline-flex';
  } else {
    authLink.textContent = 'Connexion';
    authLink.href = '#login';
    signupLink.style.display = 'inline-flex';
    navAdmin.style.display = 'none';
  }
}

function logout() {
  currentUser = null;
  authToken = null;
  adminEditingPropertyId = null;
  updateNav();
}

window.addEventListener('hashchange', navigate);
window.addEventListener('load', () => {
  updateNav();

  authLink.addEventListener('click', (event) => {
    if (currentUser && authLink.textContent === 'Déconnexion') {
      event.preventDefault();
      logout();
      window.location.hash = 'home';
    }
  });

  document.getElementById('signup-link')?.addEventListener('click', () => {
    if (currentUser) {
      window.location.hash = 'admin';
    }
  });

  navigate();
});
