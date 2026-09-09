/**
 * Kathmandu Transit — Public Bus Route Finder
 * Modern Web Frontend & Client-side Transit Engine
 */

// =============================================================================
// Default Seed Data (Kathmandu Valley) — Fallback & Instant Offline Capability
// =============================================================================
const DEFAULT_STOPS = [
  { stopId: 1, name: "Ratna Park", area: "Kathmandu", latitude: 27.7040, longitude: 85.3140 },
  { stopId: 2, name: "Sundhara", area: "Kathmandu", latitude: 27.6988, longitude: 85.3122 },
  { stopId: 3, name: "New Baneshwor", area: "Baneshwor", latitude: 27.6928, longitude: 85.3405 },
  { stopId: 4, name: "Koteshwor", area: "Koteshwor", latitude: 27.6776, longitude: 85.3486 },
  { stopId: 5, name: "Tribhuvan Airport", area: "Sinamangal", latitude: 27.6966, longitude: 85.3591 },
  { stopId: 6, name: "Kalanki", area: "Kalanki", latitude: 27.6939, longitude: 85.2809 },
  { stopId: 7, name: "Balkhu", area: "Kirtipur Road", latitude: 27.6858, longitude: 85.2966 },
  { stopId: 8, name: "Kalimati", area: "Kalimati", latitude: 27.6971, longitude: 85.3018 },
  { stopId: 9, name: "Balaju", area: "Balaju", latitude: 27.7280, longitude: 85.3050 },
  { stopId: 10, name: "Gongabu Bus Park", area: "Gongabu", latitude: 27.7326, longitude: 85.3193 },
  { stopId: 11, name: "Swayambhu", area: "Swayambhu", latitude: 27.7150, longitude: 85.2903 },
  { stopId: 12, name: "Lagankhel", area: "Lalitpur", latitude: 27.6667, longitude: 85.3235 },
  { stopId: 13, name: "Patan Dhoka", area: "Lalitpur", latitude: 27.6792, longitude: 85.3200 },
  { stopId: 14, name: "Jawalakhel", area: "Lalitpur", latitude: 27.6742, longitude: 85.3126 },
  { stopId: 15, name: "Bhaktapur Durbar Square", area: "Bhaktapur", latitude: 27.6710, longitude: 85.4285 },
  { stopId: 16, name: "Suryabinayak", area: "Bhaktapur", latitude: 27.6656, longitude: 85.4436 },
  { stopId: 17, name: "Chabahil", area: "Chabahil", latitude: 27.7175, longitude: 85.3459 },
  { stopId: 18, name: "Boudha", area: "Boudha", latitude: 27.7215, longitude: 85.3620 }
];

const DEFAULT_ROUTES = [
  { routeId: 1, routeName: "Route 1: Ratnapark - Airport - Koteshwor", operatorName: "Sajha Yatayat", farePerKm: 4.5, color: "#10b981" },
  { routeId: 2, routeName: "Route 2: Ratnapark - Kalanki - Balkhu", operatorName: "Nepal Yatayat", farePerKm: 4.0, color: "#f97316" },
  { routeId: 3, routeName: "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", operatorName: "City Bus Service", farePerKm: 4.5, color: "#0ea5e9" },
  { routeId: 4, routeName: "Route 4: Lagankhel - Patan Dhoka - Jawalakhel", operatorName: "Sajha Yatayat", farePerKm: 4.0, color: "#10b981" },
  { routeId: 5, routeName: "Route 5: Koteshwor - Chabahil - Boudha", operatorName: "Boudha Sewa Yatayat", farePerKm: 5.0, color: "#f59e0b" },
  { routeId: 6, routeName: "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", operatorName: "Bhaktapur Yatayat", farePerKm: 5.5, color: "#a855f7" }
];

const DEFAULT_SEGMENTS = [
  // Route 1
  { routeId: 1, routeName: "Route 1: Ratnapark - Airport - Koteshwor", fromStopId: 1, toStopId: 2, distanceKm: 1.2, fareNpr: 15.0, sequenceOrder: 1 },
  { routeId: 1, routeName: "Route 1: Ratnapark - Airport - Koteshwor", fromStopId: 2, toStopId: 3, distanceKm: 3.0, fareNpr: 15.0, sequenceOrder: 2 },
  { routeId: 1, routeName: "Route 1: Ratnapark - Airport - Koteshwor", fromStopId: 3, toStopId: 5, distanceKm: 2.5, fareNpr: 15.0, sequenceOrder: 3 },
  { routeId: 1, routeName: "Route 1: Ratnapark - Airport - Koteshwor", fromStopId: 5, toStopId: 4, distanceKm: 2.0, fareNpr: 15.0, sequenceOrder: 4 },
  // Route 2
  { routeId: 2, routeName: "Route 2: Ratnapark - Kalanki - Balkhu", fromStopId: 1, toStopId: 8, distanceKm: 2.8, fareNpr: 15.0, sequenceOrder: 1 },
  { routeId: 2, routeName: "Route 2: Ratnapark - Kalanki - Balkhu", fromStopId: 8, toStopId: 6, distanceKm: 2.6, fareNpr: 15.0, sequenceOrder: 2 },
  { routeId: 2, routeName: "Route 2: Ratnapark - Kalanki - Balkhu", fromStopId: 6, toStopId: 7, distanceKm: 1.8, fareNpr: 15.0, sequenceOrder: 3 },
  // Route 3
  { routeId: 3, routeName: "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", fromStopId: 10, toStopId: 9, distanceKm: 1.5, fareNpr: 15.0, sequenceOrder: 1 },
  { routeId: 3, routeName: "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", fromStopId: 9, toStopId: 11, distanceKm: 2.2, fareNpr: 15.0, sequenceOrder: 2 },
  { routeId: 3, routeName: "Route 3: Gongabu - Balaju - Swayambhu - Kalimati", fromStopId: 11, toStopId: 8, distanceKm: 2.4, fareNpr: 15.0, sequenceOrder: 3 },
  // Route 4
  { routeId: 4, routeName: "Route 4: Lagankhel - Patan Dhoka - Jawalakhel", fromStopId: 12, toStopId: 13, distanceKm: 1.6, fareNpr: 15.0, sequenceOrder: 1 },
  { routeId: 4, routeName: "Route 4: Lagankhel - Patan Dhoka - Jawalakhel", fromStopId: 13, toStopId: 14, distanceKm: 1.4, fareNpr: 15.0, sequenceOrder: 2 },
  // Route 5
  { routeId: 5, routeName: "Route 5: Koteshwor - Chabahil - Boudha", fromStopId: 4, toStopId: 17, distanceKm: 3.2, fareNpr: 20.0, sequenceOrder: 1 },
  { routeId: 5, routeName: "Route 5: Koteshwor - Chabahil - Boudha", fromStopId: 17, toStopId: 18, distanceKm: 1.8, fareNpr: 15.0, sequenceOrder: 2 },
  // Route 6
  { routeId: 6, routeName: "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", fromStopId: 1, toStopId: 3, distanceKm: 3.5, fareNpr: 20.0, sequenceOrder: 1 },
  { routeId: 6, routeName: "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", fromStopId: 3, toStopId: 4, distanceKm: 1.6, fareNpr: 15.0, sequenceOrder: 2 },
  { routeId: 6, routeName: "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", fromStopId: 4, toStopId: 16, distanceKm: 9.0, fareNpr: 35.0, sequenceOrder: 3 },
  { routeId: 6, routeName: "Route 6: Ratnapark - Baneshwor - Koteshwor - Bhaktapur", fromStopId: 16, toStopId: 15, distanceKm: 2.0, fareNpr: 15.0, sequenceOrder: 4 }
];

// =============================================================================
// App State
// =============================================================================
const state = {
  currentUser: { fullName: "Guest / Passenger", role: "PASSENGER", email: "" },
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

// =============================================================================
// Initialization
// =============================================================================
document.addEventListener("DOMContentLoaded", async () => {
  initMap();
  initTheme();
  setupEventListeners();
  await syncWithBackendOrSeed();
});

// Theme Management
function initTheme() {
  const savedTheme = localStorage.getItem("ktm_theme") || "dark";
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
    zoom: 13,
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
}

function updateMapTiles(theme) {
  if (!state.map) return;
  if (tileLayer) {
    state.map.removeLayer(tileLayer);
  }
  const tileUrl = theme === "dark"
    ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
    : "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png";

  tileLayer = L.tileLayer(tileUrl, {
    maxZoom: 19,
    attribution: '&copy; <a href="https://carto.com/">CARTO</a>, &copy; OpenStreetMap'
  }).addTo(state.map);
}

// =============================================================================
// Data Synchronization (Java Backend or In-Memory Seed Fallback)
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
    console.warn("Backend unavailable, loading local Kathmandu dataset...", err);
    state.stops = JSON.parse(localStorage.getItem("ktm_stops")) || DEFAULT_STOPS;
    state.routes = JSON.parse(localStorage.getItem("ktm_routes")) || DEFAULT_ROUTES;
    state.segments = JSON.parse(localStorage.getItem("ktm_segments")) || DEFAULT_SEGMENTS;
    state.backendOnline = false;

    statusEl.classList.remove("online");
    statusEl.classList.add("offline");
    statusEl.querySelector(".status-text").textContent = "Client Engine Active";
  }

  renderAllStopsAndRoutes();
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
  // Clear existing
  Object.values(state.stopMarkers).forEach(m => state.map.removeLayer(m));
  state.stopMarkers = {};

  state.stops.forEach(stop => {
    const icon = L.divIcon({
      className: "custom-bus-stop-icon",
      html: `<span>🚏</span>`,
      iconSize: [26, 26],
      iconAnchor: [13, 13]
    });

    const marker = L.marker([stop.latitude, stop.longitude], { icon })
      .addTo(state.map)
      .bindPopup(`
        <div style="font-family: var(--font-body); padding: 4px;">
          <h4 style="margin: 0 0 4px; font-size: 1rem; color: #10b981;">${stop.name}</h4>
          <p style="margin: 0 0 8px; font-size: 0.8rem; color: #64748b;">Area: <strong>${stop.area}</strong></p>
          <div style="display: flex; gap: 6px;">
            <button onclick="setStopChoice('from', ${stop.stopId})" style="padding: 4px 8px; font-size: 0.75rem; background: #10b981; color: white; border: none; border-radius: 4px; cursor: pointer;">Set Origin</button>
            <button onclick="setStopChoice('to', ${stop.stopId})" style="padding: 4px 8px; font-size: 0.75rem; background: #f97316; color: white; border: none; border-radius: 4px; cursor: pointer;">Set Destination</button>
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
  "City Bus Service": "#0ea5e9",
  "Boudha Sewa Yatayat": "#f59e0b",
  "Bhaktapur Yatayat": "#a855f7"
};

function renderRoutePolylines() {
  state.routeLinesLayer.clearLayers();

  state.segments.forEach(seg => {
    const fromStop = state.stops.find(s => s.stopId === seg.fromStopId);
    const toStop = state.stops.find(s => s.stopId === seg.toStopId);
    const route = state.routes.find(r => r.routeId === seg.routeId);

    if (fromStop && toStop) {
      const color = route ? (OPERATOR_COLORS[route.operatorName] || "#38bdf8") : "#38bdf8";
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
    const color = OPERATOR_COLORS[r.operatorName] || "#38bdf8";
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
  // If backend is online, request Java WebServer
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

  // Client-side Dijkstra fallback (mirroring Java RouteFinderService)
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
    // Reverse edge
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
  const distinctRoutes = new Set(edgePath.map(e => e.routeId)).size;

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
    busChanges: distinctRoutes,
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
    else if (isTransfer) { nodeClass += " transfer-node"; nodeIcon = "⇄"; }

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
          ${isTransfer ? '<div style="color: #a855f7; font-size: 0.75rem; font-weight: 700; margin-top: 4px;">⚠️ Transfer Bus Here</div>' : ''}
          ${legHtml}
        </div>
      </div>
    `;
  }).join("");

  // Map Animation & Highlight
  highlightJourneyOnMap(result);
}

function highlightJourneyOnMap(result) {
  state.activeJourneyLayer.clearLayers();

  // Reset markers
  Object.values(state.stopMarkers).forEach(m => {
    const el = m.getElement();
    if (el) {
      el.classList.remove("active-from", "active-to");
    }
  });

  const latlngs = [];
  result.stopPath.forEach((stop, i) => {
    latlngs.push([stop.latitude, stop.longitude]);
    const marker = state.stopMarkers[stop.stopId];
    if (marker) {
      const el = marker.getElement();
      if (el) {
        if (i === 0) el.classList.add("active-from");
        if (i === result.stopPath.length - 1) el.classList.add("active-to");
      }
    }
  });

  if (latlngs.length > 0) {
    // Glowing background line
    L.polyline(latlngs, {
      color: "#10b981",
      weight: 8,
      opacity: 0.4
    }).addTo(state.activeJourneyLayer);

    // Foreground path
    const pathLine = L.polyline(latlngs, {
      color: "#ffffff",
      weight: 4,
      dashArray: "6, 8",
      opacity: 0.95
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
        <button class="row-delete-btn" onclick="deleteStop(${s.stopId})" title="Delete Stop">🗑️</button>
      </div>
    `).join("");
  }

  if (routesTable) {
    routesTable.innerHTML = state.routes.map(r => `
      <div class="data-row">
        <div>
          <strong>${r.routeName}</strong>
          <div style="font-size: 0.72rem; color: var(--text-muted);">${r.operatorName} • NPR ${r.farePerKm}/km</div>
        </div>
        <button class="row-delete-btn" onclick="deleteRoute(${r.routeId})" title="Delete Route">🗑️</button>
      </div>
    `).join("");
  }
}

window.deleteStop = async function(id) {
  if (!confirm("Are you sure you want to delete this bus stop? Associated segments will also be removed.")) return;

  if (state.backendOnline) {
    try {
      await fetch(`${state.apiBase}/api/stops?id=${id}`, { method: "DELETE" });
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
      await fetch(`${state.apiBase}/api/routes?id=${id}`, { method: "DELETE" });
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
  // Navigation Tabs
  const navBtns = {
    "nav-planner": "panel-planner",
    "nav-routes": "panel-routes",
    "nav-admin": "panel-admin"
  };

  Object.entries(navBtns).forEach(([btnId, panelId]) => {
    document.getElementById(btnId).addEventListener("click", () => {
      document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
      document.getElementById(btnId).classList.add("active");

      document.querySelectorAll(".panel-section").forEach(p => p.classList.add("hidden"));
      document.getElementById(panelId).classList.remove("hidden");
    });
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

  // Admin Add Stop Form
  document.getElementById("add-stop-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("new-stop-name").value.trim();
    const area = document.getElementById("new-stop-area").value.trim();
    const lat = parseFloat(document.getElementById("new-stop-lat").value);
    const lon = parseFloat(document.getElementById("new-stop-lon").value);

    let newStop = { stopId: Date.now(), name, area, latitude: lat, longitude: lon };

    if (state.backendOnline) {
      try {
        const res = await fetch(`${state.apiBase}/api/stops`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name, area, latitude: lat, longitude: lon })
        });
        if (res.ok) newStop = await res.json();
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

  // Admin Add Route Form
  document.getElementById("add-route-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const routeName = document.getElementById("new-route-name").value.trim();
    const operatorName = document.getElementById("new-route-operator").value.trim();
    const farePerKm = parseFloat(document.getElementById("new-route-fare").value);

    let newRoute = { routeId: Date.now(), routeName, operatorName, farePerKm };

    if (state.backendOnline) {
      try {
        const res = await fetch(`${state.apiBase}/api/routes`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ routeName, operatorName, farePerKm })
        });
        if (res.ok) newRoute = await res.json();
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

  // Admin Add Segment Form
  document.getElementById("add-segment-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const routeId = parseInt(document.getElementById("segment-route-select").value);
    const fromStopId = parseInt(document.getElementById("segment-from-select").value);
    const toStopId = parseInt(document.getElementById("segment-to-select").value);
    const distanceKm = parseFloat(document.getElementById("segment-distance").value);
    const fareNpr = parseFloat(document.getElementById("segment-fare").value);
    const sequenceOrder = parseInt(document.getElementById("segment-order").value);

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
        await fetch(`${state.apiBase}/api/segments`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(seg)
        });
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
        await fetch(`${state.apiBase}/api/reset`, { method: "POST" });
      } catch (err) {
        console.warn("Reset API failed", err);
      }
    }

    localStorage.removeItem("ktm_stops");
    localStorage.removeItem("ktm_routes");
    localStorage.removeItem("ktm_segments");
    state.stops = [...DEFAULT_STOPS];
    state.routes = [...DEFAULT_ROUTES];
    state.segments = [...DEFAULT_SEGMENTS];
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
    setUser({ fullName: "Guest / Passenger", role: "PASSENGER", email: "" });
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
  document.getElementById("user-display-name").textContent = `${user.fullName} (${user.role})`;

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
  toast.innerHTML = `<span>🚌</span> <span>${msg}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(100%)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3200);
}
