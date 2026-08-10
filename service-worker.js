
const CACHE_NAME="jdr-lunaria-v2-1-3";
const CORE=[
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./icône-192.png",
  "./icône-512.png"
];

self.addEventListener("install",event=>{
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache=>cache.addAll(CORE))
      .then(()=>self.skipWaiting())
  );
});

self.addEventListener("activate",event=>{
  event.waitUntil(
    caches.keys()
      .then(keys=>Promise.all(keys.filter(k=>k!==CACHE_NAME).map(k=>caches.delete(k))))
      .then(()=>self.clients.claim())
  );
});

self.addEventListener("fetch",event=>{
  if(event.request.method!=="GET") return;

  event.respondWith(
    fetch(event.request)
      .then(response=>{
        const copy=response.clone();
        caches.open(CACHE_NAME).then(cache=>cache.put(event.request,copy)).catch(()=>{});
        return response;
      })
      .catch(()=>caches.match(event.request).then(cached=>cached || caches.match("./index.html")))
  );
});
