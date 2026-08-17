const CACHE_NAME = "jdr-lunaria-v2-2-8";
const CORE = ["/JDR/","/JDR/index.html","/JDR/manifest.webmanifest","/JDR/icône-192.png","/JDR/icône-512.png"];
self.addEventListener("install", event => {
  self.skipWaiting();
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(CORE)));
});
self.addEventListener("activate", event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key)))).then(() => self.clients.claim()));
});
self.addEventListener("message", event => {
  if(event.data && event.data.type === "SKIP_WAITING") self.skipWaiting();
});
self.addEventListener("fetch", event => {
  if(event.request.method !== "GET") return;
  if(event.request.mode === "navigate") {
    event.respondWith(fetch(event.request,{cache:"no-store"}).catch(() => caches.match("/JDR/index.html")));
    return;
  }
  if(new URL(event.request.url).origin === self.location.origin) {
    event.respondWith(caches.match(event.request).then(cached => cached || fetch(event.request)));
  }
});
