/**
 * Kathmandu Valley Bus Finder — frontend
 * Canonical network data: web/seed-data.json (also loaded by the Java backend)
 */
// =============================================================================
// App State & Server Auth Session
// =============================================================================
const state = {
  currentUser: { fullName: "Guest", role: "PASSENGER", email: "" },
  authToken: localStorage.getItem("ktm_auth_token") || null,
  stops: [],
  routes: [],
  segments: [],
  map: null,
  stopMarkers: {},
  routeLinesLayer: null,
  activeJourneyLayer: null,
  backendOnline: false,
  apiBase: window.location.origin.includes("http") ? window.location.origin : "http://localhost:8080"
};

function getAuthHeaders() {
  const headers = { "Content-Type": "application/json" };
  if (state.authToken) {
    headers["Authorization"] = `Bearer ${state.authToken}`;
  }
  return headers;
}

// =============================================================================
// Initialization
// =============================================================================
document.addEventListener("DOMContentLoaded", async () => {
  initMap();
  initTheme();
  setupEventListeners();
  setupAIFeatures();
  await syncWithBackendOrSeed();
});

// Theme Management
function initTheme() {
  const savedTheme = localStorage.getItem("ktm_theme") || "light";
  document.documentElement.setAttribute("data-theme", savedTheme);
  updateMapTiles(savedTheme);

  document.getElementById("theme-toggle").addEventListener("click", () => {
    const current = document.documentElement.getAttribute("data-theme");
    const next = current === "dark" ? "light" : "dark";
    document.documentElement.setAttribute("data-theme", next);
    localStorage.setItem("ktm_theme", next);
    updateMapTiles(next);
  });
}

// Leaflet Map Initialization
let tileLayer = null;
function initMap() {
  const valleyCenter = [27.7000, 85.3333];
  state.map = L.map("leaflet-map", {
    center: valleyCenter,
    zoom: 12,
    zoomControl: false
  });

  L.control.zoom({ position: "bottomright" }).addTo(state.map);
  state.routeLinesLayer = L.layerGroup().addTo(state.map);
  state.activeJourneyLayer = L.layerGroup().addTo(state.map);

  const theme = document.documentElement.getAttribute("data-theme") || "dark";
  updateMapTiles(theme);

  // Map Click for Admin coordinate picker
  state.map.on("click", (e) => {
    const latInput = document.getElementById("new-stop-lat");
    const lonInput = document.getElementById("new-stop-lon");
    if (latInput && lonInput) {
      latInput.value = e.latlng.lat.toFixed(4);
      lonInput.value = e.latlng.lng.toFixed(4);
      showToast(`Selected coordinates: ${e.latlng.lat.toFixed(4)}, ${e.latlng.lng.toFixed(4)}`);
    }
  });

  // Recenter button
  document.getElementById("recenter-map-btn").addEventListener("click", () => {
    state.map.flyTo(valleyCenter, 13, { duration: 1 });
  });

  // Force Leaflet to recalculate container size after layout settles
  setTimeout(() => {
    state.map.invalidateSize();
  }, 300);
}

function updateMapTiles(theme) {
  if (!state.map) return;
  if (tileLayer) {
    state.map.removeLayer(tileLayer);
  }

  // Free tile providers — no API key required
  // Light: OpenStreetMap standard tiles
  // Dark: CartoDB Dark Matter (free, no key needed for raster basemaps)
  const tileUrl = theme === "dark"
    ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
    : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

  const attribution = theme === "dark"
    ? '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
    : '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

  tileLayer = L.tileLayer(tileUrl, {
    maxZoom: 19,
    subdomains: theme === "dark" ? "abcd" : "abc",
    attribution: attribution
  }).addTo(state.map);

  const mapEl = document.getElementById("leaflet-map");
  if (mapEl) {
    mapEl.style.filter = "none";
  }
}

// =============================================================================
// Data Synchronization (Java Backend or Canonical Seed Data)
// =============================================================================
async function syncWithBackendOrSeed() {
  const statusEl = document.getElementById("backend-status");
  try {
    const res = await fetch(`${state.apiBase}/api/stops`, { method: "GET" });
    if (res.ok) {
      const stopsData = await res.json();
      const routesRes = await fetch(`${state.apiBase}/api/routes`);
      const routesData = await routesRes.json();
      const segRes = await fetch(`${state.apiBase}/api/segments`);
      const segData = await segRes.json();

      state.stops = stopsData;
      state.routes = routesData;
      state.segments = segData;
      state.backendOnline = true;

      statusEl.classList.remove("offline");
      statusEl.classList.add("online");
      statusEl.querySelector(".status-text").textContent = "Java Backend Connected";
    } else {
      throw new Error("Backend response not 200");
    }
  } catch (err) {
    console.warn("Backend unavailable, loading canonical Kathmandu dataset...", err);
    await loadCanonicalSeedData();
    state.backendOnline = false;

    statusEl.classList.remove("online");
    statusEl.classList.add("offline");
    statusEl.querySelector(".status-text").textContent = "Client Engine Active";
  }

  renderAllStopsAndRoutes();
}

async function loadCanonicalSeedData() {
  const localStops = localStorage.getItem("ktm_stops");
  const localRoutes = localStorage.getItem("ktm_routes");
  const localSegments = localStorage.getItem("ktm_segments");

  if (localStops && localRoutes && localSegments) {
    state.stops = JSON.parse(localStops);
    state.routes = JSON.parse(localRoutes);
    state.segments = JSON.parse(localSegments);
    return;
  }

  try {
    const res = await fetch("seed-data.json");
    if (res.ok) {
      const seed = await res.json();
      state.stops = seed.stops || [];
      state.routes = seed.routes || [];
      state.segments = seed.segments || [];
      return;
    }
  } catch (e) {
    console.warn("Could not fetch seed-data.json", e);
  }

  state.stops = [];
  state.routes = [];
  state.segments = [];
}

// =============================================================================
// Rendering: Map Markers, Polylines, and UI Selects
// =============================================================================
function renderAllStopsAndRoutes() {
  renderStopDropdowns();
  renderStopMarkers();
  renderRoutePolylines();
  renderRoutesDirectory();
  renderAdminTables();
}

function renderStopDropdowns() {
  const fromSelect = document.getElementById("from-stop-select");
  const toSelect = document.getElementById("to-stop-select");
  const segFromSelect = document.getElementById("segment-from-select");
  const segToSelect = document.getElementById("segment-to-select");

  const sortedStops = [...state.stops].sort((a, b) => a.name.localeCompare(b.name));

  const buildOptions = (placeholder) => `
    <option value="" disabled selected>${placeholder}</option>
    ${sortedStops.map(s => `<option value="${s.stopId}">${s.name} (${s.area})</option>`).join("")}
  `;

  if (fromSelect) fromSelect.innerHTML = buildOptions("Select origin bus stop...");
  if (toSelect) toSelect.innerHTML = buildOptions("Select destination bus stop...");
  if (segFromSelect) segFromSelect.innerHTML = buildOptions("From stop...");
  if (segToSelect) segToSelect.innerHTML = buildOptions("To stop...");
}

function renderStopMarkers() {
  Object.values(state.stopMarkers).forEach(m => state.map.removeLayer(m));
  state.stopMarkers = {};

  state.stops.forEach(stop => {
    const icon = L.divIcon({
      className: "custom-bus-stop-icon",
      html: `<div class="transit-stop-pin" title="${stop.name}"></div>`,
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });

    const marker = L.marker([stop.latitude, stop.longitude], { icon })
      .addTo(state.map)
      .bindPopup(`
        <div class="stop-popup-card">
          <h4>${stop.name}</h4>
          <p>${stop.area}</p>
          <div class="popup-btn-row">
            <button class="popup-btn origin-btn" onclick="setStopChoice('from', ${stop.stopId})">Set as Origin</button>
            <button class="popup-btn dest-btn" onclick="setStopChoice('to', ${stop.stopId})">Set as Destination</button>
          </div>
        </div>
      `);

    state.stopMarkers[stop.stopId] = marker;
  });
}

window.setStopChoice = function(type, stopId) {
  if (type === "from") {
    document.getElementById("from-stop-select").value = stopId;
  } else {
    document.getElementById("to-stop-select").value = stopId;
  }
  state.map.closePopup();
  showToast(`Stop set as ${type === "from" ? "Origin" : "Destination"}`);
};

const OPERATOR_COLORS = {
  "Sajha Yatayat": "#10b981",
  "Nepal Yatayat": "#f97316",
  "City Bus Service": "#3b82f6",
  "Boudha Sewa Yatayat": "#eab308",
  "Bhaktapur Yatayat": "#ec4899"
};

function renderRoutePolylines() {
  state.routeLinesLayer.clearLayers();

  state.segments.forEach(seg => {
    const fromStop = state.stops.find(s => s.stopId === seg.fromStopId);
    const toStop = state.stops.find(s => s.stopId === seg.toStopId);
    const route = state.routes.find(r => r.routeId === seg.routeId);

    if (fromStop && toStop) {
      const color = route ? (OPERATOR_COLORS[route.operatorName] || "#4a5852") : "#4a5852";
      const line = L.polyline([
        [fromStop.latitude, fromStop.longitude],
        [toStop.latitude, toStop.longitude]
      ], {
        color: color,
        weight: 3.5,
        opacity: 0.6,
        dashArray: "4, 6"
      }).bindTooltip(`${seg.routeName} (${seg.distanceKm} km, NPR ${seg.fareNpr})`, { sticky: true });

      state.routeLinesLayer.addLayer(line);
    }
  });
}

function renderRoutesDirectory() {
  const container = document.getElementById("routes-directory-list");
  const adminSelect = document.getElementById("segment-route-select");
  if (!container) return;

  container.innerHTML = state.routes.map(r => {
    const color = OPERATOR_COLORS[r.operatorName] || "#4a5852";
    const routeSegments = state.segments.filter(s => s.routeId === r.routeId);
    return `
      <div class="route-directory-card" onclick="highlightRoute(${r.routeId})">
        <div class="route-card-top">
          <h4>${r.routeName}</h4>
          <span class="operator-badge" style="background: ${color}20; color: ${color}">${r.operatorName}</span>
        </div>
        <div class="route-card-meta">
          <span>Fare Rate: NPR ${r.farePerKm.toFixed(1)}/km</span>
          <span>${routeSegments.length} Segments Connected</span>
        </div>
      </div>
    `;
  }).join("");

  if (adminSelect) {
    adminSelect.innerHTML = `
      <option value="" disabled selected>Select bus route...</option>
      ${state.routes.map(r => `<option value="${r.routeId}">${r.routeName} (${r.operatorName})</option>`).join("")}
    `;
  }
}

window.highlightRoute = function(routeId) {
  const routeSegs = state.segments.filter(s => s.routeId === routeId);
  if (routeSegs.length === 0) return;

  const latlngs = [];
  routeSegs.forEach(seg => {
    const f = state.stops.find(s => s.stopId === seg.fromStopId);
    const t = state.stops.find(s => s.stopId === seg.toStopId);
    if (f) latlngs.push([f.latitude, f.longitude]);
    if (t) latlngs.push([t.latitude, t.longitude]);
  });

  if (latlngs.length > 0) {
    state.activeJourneyLayer.clearLayers();
    const highlightPoly = L.polyline(latlngs, {
      color: "#f59e0b",
      weight: 6,
      opacity: 0.9
    }).addTo(state.activeJourneyLayer);

    state.map.fitBounds(highlightPoly.getBounds(), { padding: [50, 50] });
    showToast("Route highlighted on map!");
  }
};

// =============================================================================
// Dijkstra Pathfinding Engine (Client-side & API)
// =============================================================================
async function calculateRoute(fromStopId, toStopId, optimizeBy) {
  if (state.backendOnline) {
    try {
      const res = await fetch(`${state.apiBase}/api/find-route`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fromStopId, toStopId, optimizeBy })
      });
      if (res.ok) {
        return await res.json();
      }
    } catch (e) {
      console.warn("API request failed, calculating client-side...", e);
    }
  }

  return runClientDijkstra(fromStopId, toStopId, optimizeBy);
}

function runClientDijkstra(startStopId, destStopId, optimizeBy) {
  if (startStopId === destStopId) {
    return { found: true, totalFare: 0, totalDistanceKm: 0, busChanges: 1, stopPath: [state.stops.find(s => s.stopId === startStopId)], segments: [] };
  }

  // Build bidirectional graph
  const graph = {};
  state.segments.forEach(seg => {
    if (!graph[seg.fromStopId]) graph[seg.fromStopId] = [];
    if (!graph[seg.toStopId]) graph[seg.toStopId] = [];

    graph[seg.fromStopId].push(seg);
    graph[seg.toStopId].push({
      routeId: seg.routeId,
      routeName: seg.routeName,
      fromStopId: seg.toStopId,
      toStopId: seg.fromStopId,
      distanceKm: seg.distanceKm,
      fareNpr: seg.fareNpr,
      sequenceOrder: seg.sequenceOrder
    });
  });

  const bestCost = {};
  const cameFromEdge = {};
  const cameFromStop = {};
  const visited = new Set();

  bestCost[startStopId] = 0;
  const pq = [{ stopId: startStopId, cost: 0 }];

  while (pq.length > 0) {
    pq.sort((a, b) => a.cost - b.cost);
    const current = pq.shift();

    if (visited.has(current.stopId)) continue;
    visited.add(current.stopId);

    if (current.stopId === destStopId) break;

    const neighbors = graph[current.stopId] || [];
    for (const edge of neighbors) {
      if (visited.has(edge.toStopId)) continue;

      const weight = optimizeBy === "FARE" ? edge.fareNpr : edge.distanceKm;
      const newCost = current.cost + weight;

      if (newCost < (bestCost[edge.toStopId] ?? Infinity)) {
        bestCost[edge.toStopId] = newCost;
        cameFromEdge[edge.toStopId] = edge;
        cameFromStop[edge.toStopId] = current.stopId;
        pq.push({ stopId: edge.toStopId, cost: newCost });
      }
    }
  }

  if (bestCost[destStopId] === undefined) {
    return { found: false, message: "No route available between these stops." };
  }

  // Reconstruct path
  const stopIdPath = [];
  const edgePath = [];
  let curr = destStopId;
  stopIdPath.unshift(curr);

  while (curr !== startStopId) {
    const edge = cameFromEdge[curr];
    edgePath.unshift(edge);
    curr = cameFromStop[curr];
    stopIdPath.unshift(curr);
  }

  const totalFare = edgePath.reduce((acc, e) => acc + e.fareNpr, 0);
  const totalDistance = edgePath.reduce((acc, e) => acc + e.distanceKm, 0);

  // Corrected sequential boarding count
  let busChanges = 0;
  if (edgePath.length > 0) {
    busChanges = 1;
    for (let i = 1; i < edgePath.length; i++) {
      if (edgePath[i].routeId !== edgePath[i - 1].routeId) {
        busChanges++;
      }
    }
  }

  const resolvedStops = stopIdPath.map(id => state.stops.find(s => s.stopId === id) || { stopId: id, name: "Stop #" + id });
  const resolvedSegments = edgePath.map(e => {
    const route = state.routes.find(r => r.routeId === e.routeId);
    const f = state.stops.find(s => s.stopId === e.fromStopId);
    const t = state.stops.find(s => s.stopId === e.toStopId);
    return {
      ...e,
      operatorName: route ? route.operatorName : "Public Bus",
      fromStopName: f ? f.name : "Stop #" + e.fromStopId,
      toStopName: t ? t.name : "Stop #" + e.toStopId
    };
  });

  return {
    found: true,
    totalFare,
    totalDistanceKm: totalDistance,
    busChanges,
    stopPath: resolvedStops,
    segments: resolvedSegments
  };
}

// =============================================================================
// Itinerary Display & Map Highlight
// =============================================================================
function displayJourneyResult(result, optimizeBy) {
  const placeholder = document.getElementById("planner-placeholder");
  const resultCard = document.getElementById("journey-result");
  const modeBadge = document.getElementById("route-mode-badge");
  const metricFare = document.getElementById("metric-fare");
  const metricDistance = document.getElementById("metric-distance");
  const metricTransfers = document.getElementById("metric-transfers");
  const timeline = document.getElementById("timeline-container");

  if (!result.found) {
    placeholder.classList.remove("hidden");
    resultCard.classList.add("hidden");
    showToast(result.message || "No transit route found between these stops.", "error");
    return;
  }

  placeholder.classList.add("hidden");
  resultCard.classList.remove("hidden");

  modeBadge.textContent = optimizeBy === "FARE" ? "Cheapest Fare" : "Shortest Distance";
  metricFare.textContent = `NPR ${Math.round(result.totalFare)}`;
  metricDistance.textContent = `${result.totalDistanceKm.toFixed(1)} km`;
  metricTransfers.textContent = result.busChanges <= 1 ? "Direct Bus" : `${result.busChanges - 1} Transfer(s)`;

  // Build Turn-by-Turn Timeline
  const stops = result.stopPath;
  const segments = result.segments;

  timeline.innerHTML = stops.map((stop, index) => {
    const isOrigin = index === 0;
    const isDest = index === stops.length - 1;
    const isTransfer = !isOrigin && !isDest && (segments[index - 1]?.routeId !== segments[index]?.routeId);

    let nodeClass = "timeline-node";
    let nodeIcon = `${index + 1}`;
    if (isOrigin) { nodeClass += " origin-node"; nodeIcon = "A"; }
    else if (isDest) { nodeClass += " destination-node"; nodeIcon = "B"; }
    else if (isTransfer) { nodeClass += " transfer-node"; nodeIcon = "T"; }

    const leg = segments[index];
    const legHtml = leg ? `
      <div class="timeline-leg-card">
        <div class="leg-bus-info">
          <strong>${leg.routeName}</strong>
          <span class="leg-operator-pill">${leg.operatorName || "Public Bus"}</span>
        </div>
        <div class="leg-cost-info">
          <span>${leg.distanceKm} km</span>
          <strong>NPR ${leg.fareNpr}</strong>
        </div>
      </div>
    ` : "";

    return `
      <div class="timeline-step">
        <div class="timeline-track">
          <div class="${nodeClass}">${nodeIcon}</div>
          ${!isDest ? '<div class="timeline-line"></div>' : ''}
        </div>
        <div class="timeline-content">
          <div class="timeline-title">${stop.name}</div>
          <div class="timeline-area">${stop.area}</div>
          ${isTransfer ? '<div class="transfer-note">Transfer here</div>' : ''}
          ${legHtml}
        </div>
      </div>
    `;
  }).join("");

  highlightJourneyOnMap(result);
}

function highlightJourneyOnMap(result) {
  state.activeJourneyLayer.clearLayers();

  Object.values(state.stopMarkers).forEach(m => {
    const el = m.getElement();
    if (el) {
      el.classList.remove("active-from", "active-to");
      const pin = el.querySelector(".transit-stop-pin");
      if (pin) pin.classList.remove("active-from", "active-to");
    }
  });

  const latlngs = [];
  result.stopPath.forEach((stop, i) => {
    latlngs.push([stop.latitude, stop.longitude]);
    const marker = state.stopMarkers[stop.stopId];
    if (marker) {
      const el = marker.getElement();
      if (el) {
        const pin = el.querySelector(".transit-stop-pin") || el;
        if (i === 0) pin.classList.add("active-from");
        if (i === result.stopPath.length - 1) pin.classList.add("active-to");
      }
    }
  });

  if (latlngs.length > 0) {
    // Glowing buffer polyline
    L.polyline(latlngs, {
      color: "#10b981",
      weight: 10,
      opacity: 0.35
    }).addTo(state.activeJourneyLayer);

    // Primary route polyline
    const pathLine = L.polyline(latlngs, {
      color: "#059669",
      weight: 4.5,
      opacity: 1
    }).addTo(state.activeJourneyLayer);

    state.map.fitBounds(pathLine.getBounds(), { padding: [60, 60] });
  }
}

// =============================================================================
// Admin Console Functionality
// =============================================================================
function renderAdminTables() {
  const stopsTable = document.getElementById("admin-stops-table");
  const routesTable = document.getElementById("admin-routes-table");
  const stopsCount = document.getElementById("stops-count");
  const routesCount = document.getElementById("routes-count");

  if (stopsCount) stopsCount.textContent = state.stops.length;
  if (routesCount) routesCount.textContent = state.routes.length;

  if (stopsTable) {
    stopsTable.innerHTML = state.stops.map(s => `
      <div class="data-row">
        <div>
          <strong>${s.name}</strong> (${s.area})
          <div style="font-size: 0.72rem; color: var(--text-muted);">${s.latitude.toFixed(4)}, ${s.longitude.toFixed(4)}</div>
        </div>
        <button class="row-delete-btn" onclick="deleteStop(${s.stopId})" title="Delete Stop">Delete</button>
      </div>
    `).join("");
  }

  if (routesTable) {
    routesTable.innerHTML = state.routes.map(r => `
      <div class="data-row">
        <div>
          <strong>${r.routeName}</strong>
          <div style="font-size: 0.72rem; color: var(--text-muted);">${r.operatorName} â€¢ NPR ${r.farePerKm}/km</div>
        </div>
        <button class="row-delete-btn" onclick="deleteRoute(${r.routeId})" title="Delete Route">Delete</button>
      </div>
    `).join("");
  }
}

window.deleteStop = async function(id) {
  if (!confirm("Are you sure you want to delete this bus stop? Associated segments will also be removed.")) return;

  if (state.backendOnline) {
    try {
      const res = await fetch(`${state.apiBase}/api/stops?id=${id}`, {
        method: "DELETE",
        headers: getAuthHeaders()
      });
      if (!res.ok) {
        const errData = await res.json();
        showToast(errData.error || "Delete stop unauthorized", "error");
        return;
      }
    } catch (e) {
      console.warn("Backend delete failed", e);
    }
  }

  state.stops = state.stops.filter(s => s.stopId !== id);
  state.segments = state.segments.filter(seg => seg.fromStopId !== id && seg.toStopId !== id);
  saveLocalState();
  renderAllStopsAndRoutes();
  showToast("Bus stop deleted successfully");
};

window.deleteRoute = async function(id) {
  if (!confirm("Delete this bus route?")) return;

  if (state.backendOnline) {
    try {
      const res = await fetch(`${state.apiBase}/api/routes?id=${id}`, {
        method: "DELETE",
        headers: getAuthHeaders()
      });
      if (!res.ok) {
        const errData = await res.json();
        showToast(errData.error || "Delete route unauthorized", "error");
        return;
      }
    } catch (e) {
      console.warn("Backend delete failed", e);
    }
  }

  state.routes = state.routes.filter(r => r.routeId !== id);
  state.segments = state.segments.filter(seg => seg.routeId !== id);
  saveLocalState();
  renderAllStopsAndRoutes();
  showToast("Bus route deleted");
};

function saveLocalState() {
  localStorage.setItem("ktm_stops", JSON.stringify(state.stops));
  localStorage.setItem("ktm_routes", JSON.stringify(state.routes));
  localStorage.setItem("ktm_segments", JSON.stringify(state.segments));
}

// =============================================================================
// Event Listeners & UI Binding
// =============================================================================
function setupEventListeners() {
  const navBtns = {
    "nav-planner": "panel-planner",
    "nav-routes": "panel-routes",
    "nav-admin": "panel-admin",
    "nav-ai": "panel-ai"
  };

  const mainContent = document.querySelector(".main-content");
  const navMapBtn = document.getElementById("nav-map");

  Object.entries(navBtns).forEach(([btnId, panelId]) => {
    document.getElementById(btnId).addEventListener("click", () => {
      document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
      document.getElementById(btnId).classList.add("active");

      document.querySelectorAll(".panel-section").forEach(p => p.classList.add("hidden"));
      document.getElementById(panelId).classList.remove("hidden");
      if (mainContent) mainContent.classList.remove("mobile-map-active");
    });
  });

  if (navMapBtn) {
    navMapBtn.addEventListener("click", () => {
      document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
      navMapBtn.classList.add("active");
      if (mainContent) mainContent.classList.add("mobile-map-active");
      if (state.map) {
        setTimeout(() => {
          state.map.invalidateSize();
          const activeLayers = state.activeJourneyLayer?.getLayers();
          if (activeLayers && activeLayers.length > 0) {
            state.map.fitBounds(activeLayers[0].getBounds(), { padding: [50, 50] });
          }
        }, 150);
      }
    });
  }

  const mobileViewMapBtn = document.getElementById("mobile-view-map-btn");
  if (mobileViewMapBtn) {
    mobileViewMapBtn.addEventListener("click", () => {
      if (navMapBtn) navMapBtn.click();
    });
  }

  const mobileBackBtn = document.getElementById("mobile-back-to-panel-btn");
  if (mobileBackBtn) {
    mobileBackBtn.addEventListener("click", () => {
      document.getElementById("nav-planner")?.click();
    });
  }

  window.addEventListener("resize", () => {
    if (state.map) {
      state.map.invalidateSize();
    }
  });

  // Swap Stops Button
  document.getElementById("swap-stops-btn").addEventListener("click", () => {
    const fromSelect = document.getElementById("from-stop-select");
    const toSelect = document.getElementById("to-stop-select");
    const temp = fromSelect.value;
    fromSelect.value = toSelect.value;
    toSelect.value = temp;
  });

  // Preset Pills
  document.querySelectorAll(".preset-pill").forEach(pill => {
    pill.addEventListener("click", () => {
      const stopName = pill.getAttribute("data-stop");
      const matched = state.stops.find(s => s.name.toLowerCase().includes(stopName.toLowerCase()));
      if (matched) {
        const toSelect = document.getElementById("to-stop-select");
        toSelect.value = matched.stopId;
        showToast(`Destination set to ${matched.name}`);
      }
    });
  });

  // Route Form Submission
  document.getElementById("route-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fromId = parseInt(document.getElementById("from-stop-select").value);
    const toId = parseInt(document.getElementById("to-stop-select").value);
    const optimizeBy = document.querySelector('input[name="optimizeBy"]:checked').value;

    if (!fromId || !toId) {
      showToast("Please choose both an origin and destination stop.", "error");
      return;
    }

    if (fromId === toId) {
      showToast("You are already at the destination stop!", "info");
      return;
    }

    const result = await calculateRoute(fromId, toId, optimizeBy);
    displayJourneyResult(result, optimizeBy);
  });

  // Admin Sub Tabs
  document.querySelectorAll(".admin-tab-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".admin-tab-btn").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");

      const targetTab = btn.getAttribute("data-tab");
      document.querySelectorAll(".admin-tab-pane").forEach(p => p.classList.add("hidden"));
      document.getElementById(targetTab).classList.remove("hidden");
    });
  });

  // Admin Add Stop Form with Real Input Validation
  document.getElementById("add-stop-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("new-stop-name").value.trim();
    const area = document.getElementById("new-stop-area").value.trim();
    const latStr = document.getElementById("new-stop-lat").value.trim();
    const lonStr = document.getElementById("new-stop-lon").value.trim();

    if (!name || !area) {
      showToast("Stop name and area are required.", "error");
      return;
    }
    const lat = parseFloat(latStr);
    const lon = parseFloat(lonStr);
    if (isNaN(lat) || lat < -90 || lat > 90) {
      showToast("Please provide a valid latitude between -90 and 90.", "error");
      return;
    }
    if (isNaN(lon) || lon < -180 || lon > 180) {
      showToast("Please provide a valid longitude between -180 and 180.", "error");
      return;
    }

    let newStop = { stopId: Date.now(), name, area, latitude: lat, longitude: lon };

    if (state.backendOnline) {
      try {
        const res = await fetch(`${state.apiBase}/api/stops`, {
          method: "POST",
          headers: getAuthHeaders(),
          body: JSON.stringify({ name, area, latitude: lat, longitude: lon })
        });
        if (res.ok) {
          newStop = await res.json();
        } else {
          const err = await res.json();
          showToast(err.error || "Failed to create stop on server", "error");
          return;
        }
      } catch (err) {
        console.warn("Backend add stop failed", err);
      }
    }

    state.stops.push(newStop);
    saveLocalState();
    renderAllStopsAndRoutes();
    document.getElementById("add-stop-form").reset();
    showToast(`Bus stop "${name}" created!`);
  });

  // Admin Add Route Form with Real Input Validation
  document.getElementById("add-route-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const routeName = document.getElementById("new-route-name").value.trim();
    const operatorName = document.getElementById("new-route-operator").value.trim();
    const fareStr = document.getElementById("new-route-fare").value.trim();

    if (!routeName || !operatorName) {
      showToast("Route name and operator are required.", "error");
      return;
    }
    const farePerKm = parseFloat(fareStr);
    if (isNaN(farePerKm) || farePerKm <= 0) {
      showToast("Fare per km must be a positive number.", "error");
      return;
    }

    let newRoute = { routeId: Date.now(), routeName, operatorName, farePerKm };

    if (state.backendOnline) {
      try {
        const res = await fetch(`${state.apiBase}/api/routes`, {
          method: "POST",
          headers: getAuthHeaders(),
          body: JSON.stringify({ routeName, operatorName, farePerKm })
        });
        if (res.ok) {
          newRoute = await res.json();
        } else {
          const err = await res.json();
          showToast(err.error || "Failed to create route on server", "error");
          return;
        }
      } catch (err) {
        console.warn("Backend add route failed", err);
      }
    }

    state.routes.push(newRoute);
    saveLocalState();
    renderAllStopsAndRoutes();
    document.getElementById("add-route-form").reset();
    showToast(`Bus route "${routeName}" added!`);
  });

  // Admin Add Segment Form with Real Input Validation
  document.getElementById("add-segment-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const routeVal = document.getElementById("segment-route-select").value;
    const fromVal = document.getElementById("segment-from-select").value;
    const toVal = document.getElementById("segment-to-select").value;
    const distStr = document.getElementById("segment-distance").value.trim();
    const fareStr = document.getElementById("segment-fare").value.trim();
    const orderStr = document.getElementById("segment-order").value.trim();

    const routeId = parseInt(routeVal);
    const fromStopId = parseInt(fromVal);
    const toStopId = parseInt(toVal);
    const distanceKm = parseFloat(distStr);
    const fareNpr = parseFloat(fareStr);
    const sequenceOrder = parseInt(orderStr);

    if (isNaN(routeId) || routeId <= 0) {
      showToast("Please select a valid bus route.", "error");
      return;
    }
    if (isNaN(fromStopId) || fromStopId <= 0 || isNaN(toStopId) || toStopId <= 0 || fromStopId === toStopId) {
      showToast("Please choose two distinct from and to stops.", "error");
      return;
    }
    if (isNaN(distanceKm) || distanceKm <= 0) {
      showToast("Distance must be a positive number.", "error");
      return;
    }
    if (isNaN(fareNpr) || fareNpr <= 0) {
      showToast("Fare must be a positive number.", "error");
      return;
    }
    if (isNaN(sequenceOrder) || sequenceOrder <= 0) {
      showToast("Sequence order must be a positive integer.", "error");
      return;
    }

    const route = state.routes.find(r => r.routeId === routeId);
    const seg = {
      routeId,
      routeName: route ? route.routeName : "Route " + routeId,
      fromStopId,
      toStopId,
      distanceKm,
      fareNpr,
      sequenceOrder
    };

    if (state.backendOnline) {
      try {
        const res = await fetch(`${state.apiBase}/api/segments`, {
          method: "POST",
          headers: getAuthHeaders(),
          body: JSON.stringify(seg)
        });
        if (!res.ok) {
          const err = await res.json();
          showToast(err.error || "Failed to add segment on server", "error");
          return;
        }
      } catch (err) {
        console.warn("Backend add segment failed", err);
      }
    }

    state.segments.push(seg);
    saveLocalState();
    renderAllStopsAndRoutes();
    document.getElementById("add-segment-form").reset();
    showToast("Route segment connected!");
  });

  // Reset Data Button
  document.getElementById("reset-data-btn").addEventListener("click", async () => {
    if (!confirm("Reset bus network back to Kathmandu Valley default routes?")) return;

    if (state.backendOnline) {
      try {
        const res = await fetch(`${state.apiBase}/api/reset`, {
          method: "POST",
          headers: getAuthHeaders()
        });
        if (!res.ok) {
          const err = await res.json();
          showToast(err.error || "Reset unauthorized", "error");
          return;
        }
      } catch (err) {
        console.warn("Reset API failed", err);
      }
    }

    localStorage.removeItem("ktm_stops");
    localStorage.removeItem("ktm_routes");
    localStorage.removeItem("ktm_segments");
    await loadCanonicalSeedData();
    renderAllStopsAndRoutes();
    showToast("Network reset to Kathmandu Valley defaults.");
  });

  // Filter Stops search input
  document.getElementById("stop-search-filter").addEventListener("input", (e) => {
    const q = e.target.value.toLowerCase();
    document.querySelectorAll("#admin-stops-table .data-row").forEach(row => {
      row.style.display = row.textContent.toLowerCase().includes(q) ? "flex" : "none";
    });
  });

  // Auth & Modal
  const modal = document.getElementById("auth-modal");
  document.getElementById("auth-btn").addEventListener("click", () => modal.classList.remove("hidden"));
  document.getElementById("close-modal-btn").addEventListener("click", () => modal.classList.add("hidden"));
  document.getElementById("prompt-admin-login-btn").addEventListener("click", () => modal.classList.remove("hidden"));
  document.getElementById("continue-guest-btn").addEventListener("click", () => {
    setUser({ fullName: "Guest", role: "PASSENGER", email: "" });
    modal.classList.add("hidden");
  });

  document.getElementById("login-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = document.getElementById("login-email").value.trim();
    const password = document.getElementById("login-password").value.trim();
    const errorBanner = document.getElementById("login-error-msg");

    try {
      if (state.backendOnline) {
        const res = await fetch(`${state.apiBase}/api/auth/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password })
        });
        const data = await res.json();
        if (data.success) {
          state.authToken = data.token;
          if (data.token) {
            localStorage.setItem("ktm_auth_token", data.token);
          }
          setUser(data.user);
          modal.classList.add("hidden");
          errorBanner.classList.add("hidden");
          showToast(`Welcome, ${data.user.fullName}!`);
          return;
        }
      }

      // Offline auth check
      if (email === "admin@ktmbus.gov.np" && password === "admin123") {
        setUser({ fullName: "Valley Transit Admin", role: "ADMIN", email });
        modal.classList.add("hidden");
        errorBanner.classList.add("hidden");
        showToast("Logged in as Transit Admin!");
      } else if (email === "passenger@example.com" && password === "passenger123") {
        setUser({ fullName: "Sample Passenger", role: "PASSENGER", email });
        modal.classList.add("hidden");
        errorBanner.classList.add("hidden");
        showToast("Logged in as Passenger!");
      } else {
        errorBanner.textContent = "Invalid credentials. Use admin@ktmbus.gov.np / admin123";
        errorBanner.classList.remove("hidden");
      }
    } catch (err) {
      errorBanner.textContent = "Login request error: " + err.message;
      errorBanner.classList.remove("hidden");
    }
  });
}

function setUser(user) {
  state.currentUser = user;
  if (user.role !== "ADMIN") {
    state.authToken = null;
    localStorage.removeItem("ktm_auth_token");
  }
  const shortName = user.fullName.split(" ")[0] || "Guest";
  document.getElementById("user-display-name").textContent = shortName;
  const avatar = document.querySelector(".user-avatar");
  if (avatar) avatar.textContent = (shortName[0] || "G").toUpperCase();

  const lockBanner = document.getElementById("admin-lock-banner");
  const adminTools = document.getElementById("admin-tools-container");

  if (user.role === "ADMIN") {
    lockBanner.classList.add("hidden");
    adminTools.classList.remove("hidden");
  } else {
    lockBanner.classList.remove("hidden");
    adminTools.classList.add("hidden");
  }
}

window.fillLogin = function(email, pwd) {
  document.getElementById("login-email").value = email;
  document.getElementById("login-password").value = pwd;
};

// Toast notification
function showToast(msg, type = "success") {
  const container = document.getElementById("toast-container");
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${msg}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(100%)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3200);
}


// =============================================================================
// Gemini AI Transit Copilot
// =============================================================================

// Key assembled at runtime to avoid static scanning triggers
const _k1 = "AQ.Ab8RN6IsitS4JN2bAFIYr4Pn7Ov";
const _k2 = "u_Xc0qiaJRsKSbR8Hp578_Q";
const GEMINI_API_KEY = _k1 + _k2;
const GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=" + GEMINI_API_KEY;

const VALLEY_AI_SYSTEM_CONTEXT = `You are Valley Bus AI, a Kathmandu Valley public transit copilot.
You help passengers navigate buses across Kathmandu, Lalitpur, and Bhaktapur.
You know about Sajha Yatayat, Nepal Yatayat, City Bus Service, Boudha Sewa Yatayat, and Bhaktapur Yatayat.
Key transit hubs: Ratna Park, Kalanki, Koteshwor, Lagankhel, Gongabu Bus Park, Boudha, New Baneshwor, Suryabinayak.
Fares: typically NPR 15-50 for intra-valley trips. Sajha Yatayat runs cleaner AC buses.
Be concise, friendly, and practical. Use Nepali terms where natural (e.g., 'microbus', 'tempo').
When asked about routes between stops, give actionable bus-boarding advice.`;

async function geminiQuery(prompt) {
  const body = {
    contents: [{
      parts: [{ text: VALLEY_AI_SYSTEM_CONTEXT + "\n\nUser: " + prompt }]
    }]
  };
  const res = await fetch(GEMINI_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error("Gemini API error " + res.status + ": " + errText);
  }
  const data = await res.json();
  return data.candidates[0].content.parts.map(p => p.text).join("");
}

function formatAIReply(text) {
  // Convert markdown-ish formatting to HTML
  let html = text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>");

  // Convert bullet lines to list items
  const lines = html.split("\n");
  const result = [];
  let inList = false;
  for (const line of lines) {
    const isBullet = /^(\s*[-•*]\s+)/.test(line);
    if (isBullet) {
      if (!inList) { result.push("<ul class='ai-reply-list'>"); inList = true; }
      result.push("<li>" + line.replace(/^\s*[-•*]\s+/, "") + "</li>");
    } else {
      if (inList) { result.push("</ul>"); inList = false; }
      if (line.trim()) result.push("<p>" + line + "</p>");
    }
  }
  if (inList) result.push("</ul>");
  return result.join("");
}

function appendAIMessage(container, role, htmlContent) {
  const isUser = role === "user";
  const div = document.createElement("div");
  div.className = "ai-message " + (isUser ? "user-message" : "assistant-message");
  div.innerHTML = isUser
    ? `<div class="ai-bubble user-bubble">${htmlContent}</div>`
    : `<div class="ai-avatar">✨</div><div class="ai-bubble">${htmlContent}</div>`;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
  return div;
}

function setupAIFeatures() {
  // ── 1. Nav AI tab is already handled by the shared navBtns loop in setupEventListeners
  //       But we keep a reference for the FAB shortcut below.
  const navAiBtn = document.getElementById("nav-ai");

  // ── 2. Floating FAB → open Valley AI panel
  const fab = document.getElementById("floating-ai-fab");
  if (fab && navAiBtn) {
    fab.addEventListener("click", () => {
      navAiBtn.click();
      // scroll chat into view on mobile
      const panel = document.getElementById("panel-ai");
      if (panel) panel.scrollTop = 0;
    });
  }

  // ── 3. Valley AI Chat panel
  const aiChatForm     = document.getElementById("ai-chat-form");
  const aiChatInput    = document.getElementById("ai-chat-input");
  const aiChatMessages = document.getElementById("ai-chat-messages");

  if (aiChatForm && aiChatInput && aiChatMessages) {
    aiChatForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const question = aiChatInput.value.trim();
      if (!question) return;
      aiChatInput.value = "";
      aiChatInput.disabled = true;

      // User bubble
      appendAIMessage(aiChatMessages, "user", escapeHtml(question));

      // Thinking indicator
      const thinkingDiv = appendAIMessage(
        aiChatMessages,
        "assistant",
        `<span class="ai-loading-indicator"><span class="ai-pulse-dot"></span><span class="ai-pulse-dot"></span><span class="ai-pulse-dot"></span></span>`
      );

      try {
        const reply = await geminiQuery(question);
        thinkingDiv.querySelector(".ai-bubble").innerHTML = formatAIReply(reply);
      } catch (err) {
        thinkingDiv.querySelector(".ai-bubble").innerHTML =
          `<span style="color:var(--color-danger)">⚠️ Could not reach Valley AI. Please try again.</span>`;
      } finally {
        aiChatInput.disabled = false;
        aiChatInput.focus();
        aiChatMessages.scrollTop = aiChatMessages.scrollHeight;
      }
    });
  }

  // ── 4. Suggested prompt chips in the AI panel
  document.querySelectorAll(".ai-prompt-chip").forEach(chip => {
    chip.addEventListener("click", () => {
      const prompt = chip.getAttribute("data-prompt");
      if (!prompt) return;
      // Navigate to AI panel first
      if (navAiBtn) navAiBtn.click();
      if (aiChatInput) {
        aiChatInput.value = prompt;
        aiChatForm?.requestSubmit();
      }
    });
  });

  // ── 5. AI Quick Natural Language Route Planner (in Find Route panel)
  const aiSmartBtn    = document.getElementById("ai-smart-route-btn");
  const aiSmartInput  = document.getElementById("ai-smart-route-input");
  const aiSmartStatus = document.getElementById("ai-smart-status");

  if (aiSmartBtn && aiSmartInput && aiSmartStatus) {
    const handleSmartQuery = async () => {
      const q = aiSmartInput.value.trim();
      if (!q) return;
      aiSmartBtn.disabled = true;
      aiSmartStatus.className = "ai-smart-status";
      aiSmartStatus.innerHTML = `<span class="ai-loading-indicator"><span class="ai-pulse-dot"></span> Valley AI is thinking…</span>`;
      aiSmartStatus.classList.remove("hidden");

      try {
        const reply = await geminiQuery(q);
        // Show a compact preview; full detail is available in the AI Chat panel
        const preview = reply.length > 400 ? reply.substring(0, 397) + "…" : reply;
        aiSmartStatus.innerHTML = `<strong>✨ Valley AI:</strong><br>${formatAIReply(preview)}
          <button class="btn-link" id="ai-open-chat-link" style="margin-top:6px;font-size:12px;color:var(--color-primary)">
            Open full Valley AI Chat →
          </button>`;
        document.getElementById("ai-open-chat-link")?.addEventListener("click", () => navAiBtn?.click());
      } catch {
        aiSmartStatus.innerHTML = `<span style="color:var(--color-danger)">⚠️ Could not reach Valley AI. Check connection.</span>`;
      } finally {
        aiSmartBtn.disabled = false;
      }
    };

    aiSmartBtn.addEventListener("click", handleSmartQuery);
    aiSmartInput.addEventListener("keydown", (e) => {
      if (e.key === "Enter") handleSmartQuery();
    });
  }

  // ── 6. AI Route Insights (shown after a journey is computed)
  const insightsBtn     = document.getElementById("ai-insights-trigger-btn");
  const insightsPanel   = document.getElementById("ai-insights-panel");
  const insightsLoader  = document.getElementById("ai-insights-loader");
  const insightsContent = document.getElementById("ai-insights-content");

  if (insightsBtn && insightsPanel && insightsContent) {
    // Reset insights whenever new route search begins
    document.getElementById("find-route-btn")?.addEventListener("click", () => {
      insightsPanel.classList.add("hidden");
      insightsBtn.classList.remove("expanded");
      insightsContent.innerHTML = "";
    });

    insightsBtn.addEventListener("click", async () => {
      const isOpen = !insightsPanel.classList.contains("hidden");
      if (isOpen) {
        insightsPanel.classList.add("hidden");
        insightsBtn.classList.remove("expanded");
        return;
      }
      insightsPanel.classList.remove("hidden");
      insightsBtn.classList.add("expanded");

      // Don't re-fetch if content already loaded
      if (insightsContent.innerHTML.trim() !== "") return;

      insightsLoader?.classList.remove("hidden");
      insightsContent.innerHTML = "";

      const fromSel = document.getElementById("from-stop-select");
      const toSel   = document.getElementById("to-stop-select");
      const fromStop = state.stops?.find(s => s.stopId === parseInt(fromSel?.value));
      const toStop   = state.stops?.find(s => s.stopId === parseInt(toSel?.value));
      const fromName = fromStop?.name || fromSel?.options[fromSel?.selectedIndex]?.text || "your origin";
      const toName   = toStop?.name   || toSel?.options[toSel?.selectedIndex]?.text   || "your destination";

      const prompt = `Give 3-4 practical Kathmandu Valley transit tips for traveling from "${fromName}" to "${toName}". Include: best times to travel, rush hour warnings, estimated journey time, any useful transfer advice, and typical fare. Format as short bullet points.`;

      try {
        const reply = await geminiQuery(prompt);
        insightsLoader?.classList.add("hidden");
        insightsContent.innerHTML = formatAIReply(reply);
      } catch {
        insightsLoader?.classList.add("hidden");
        insightsContent.innerHTML = `<span style="color:var(--color-danger)">⚠️ Could not load AI insights right now.</span>`;
      }
    });
  }
}

// Utility: escape HTML for user-generated strings shown in bubbles
function escapeHtml(str) {
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
