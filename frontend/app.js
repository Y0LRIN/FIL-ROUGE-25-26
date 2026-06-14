const app = document.getElementById('app');
const authLink = document.getElementById('auth-link');
let currentUser = null;

const routes = {
  home: renderHome,
  search: renderSearch,
  annonce: renderAnnonce,
  login: renderLogin,
  favorites: renderFavorites,
  admin: renderAdmin,
};

function navigate() {
  const hash = window.location.hash.slice(1) || 'home';
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
      <div>
        <h1 id="home-title">Ymmo — la recherche immobilière simplifiée</h1>
        <p>Explorez les meilleures annonces, sauvegardez vos favoris et administrez facilement les biens de votre portefeuille.</p>
        <div class="cta-group">
          <a class="button button-primary" href="#search">Rechercher</a>
          <a class="button button-secondary" href="#annonce">Voir une annonce</a>
        </div>
      </div>
      <div aria-hidden="true">
        <div class="card" style="padding: 1.5rem; background: #eef2ff;">
          <h2>Performances</h2>
          <p>Prototype responsive et accessible, conçu sans framework pour des chargements rapides.</p>
        </div>
      </div>
    </section>
    <section class="page-section" aria-labelledby="services-title">
      <h2 id="services-title">Ce que propose Ymmo</h2>
      <div class="grid grid-3">
        <article class="card"><h3>Recherche rapide</h3><p>Trouver des biens par type, statut, prix et ville.</p></article>
        <article class="card"><h3>Favoris</h3><p>Sauvegarder vos annonces préférées pour y revenir plus tard.</p></article>
        <article class="card"><h3>Administration</h3><p>Gérer les annonces, contrats, visites et transactions depuis un tableau dédié.</p></article>
      </div>
    </section>
  `;
}

function renderSearch() {
  app.innerHTML = `
    <section class="page-section" aria-labelledby="search-title">
      <header class="grid" style="gap: 1rem; align-items: start;">
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
              <option value="COMMERCIAL">Local</option>
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
      </form>
    </section>
  `;

  document.getElementById('login-form').addEventListener('submit', (event) => {
    event.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    currentUser = email ? { email } : null;
    document.getElementById('login-message').textContent = currentUser ? 'Connexion réussie.' : 'Veuillez saisir une adresse e-mail.';
    if (currentUser) {
      authLink.textContent = 'Déconnexion';
      authLink.href = '#home';
      window.location.hash = 'home';
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
  app.innerHTML = `
    <section class="page-section" aria-labelledby="admin-title">
      <h2 id="admin-title">Panneau administrateur</h2>
      <p>Créez et modifiez des annonces, et consultez les contrats, visites et transactions.</p>
      <div class="grid grid-3">
        <div class="form-card">
          <h3>Créer une annonce</h3>
          <div class="field-group">
            <label>Titre</label>
            <input id="admin-title-field" type="text" placeholder="Maison de village" />
          </div>
          <div class="field-group">
            <label>Description</label>
            <textarea id="admin-description-field" placeholder="Description du bien"></textarea>
          </div>
          <div class="field-group">
            <label>Prix</label>
            <input id="admin-price-field" type="number" placeholder="230000" />
          </div>
          <button class="button button-primary" id="admin-create">Créer</button>
          <p id="admin-message" class="listing-meta"></p>
        </div>
        <div class="card">
          <h3>Contrats</h3>
          <p>Suivi des contrats signés.</p>
        </div>
        <div class="card">
          <h3>Visites</h3>
          <p>Consultez les retours de visite et planifiez les rendez-vous.</p>
        </div>
      </div>
    </section>
  `;

  document.getElementById('admin-create').addEventListener('click', () => {
    document.getElementById('admin-message').textContent = 'La création depuis l\'interface est un prototype. Connectez le backend pour sauvegarder les annonces.';
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

window.addEventListener('hashchange', navigate);
window.addEventListener('load', () => {
  authLink.addEventListener('click', (event) => {
    if (currentUser && authLink.textContent === 'Déconnexion') {
      event.preventDefault();
      currentUser = null;
      authLink.textContent = 'Connexion';
      window.location.hash = 'home';
    }
  });
  navigate();
});
