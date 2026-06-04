// Живые подсказки для поиска (автодополнение)
(function () {
  var input = document.getElementById('search-input');
  var box = document.getElementById('search-suggest');
  if (!input || !box) return;

  var url = input.getAttribute('data-suggest');
  var ctx = input.getAttribute('data-ctx') || '';
  var timer = null, items = [], active = -1, lastQuery = '';

  function close() {
    box.innerHTML = '';
    box.style.display = 'none';
    items = [];
    active = -1;
  }

  function render(data) {
    items = data || [];
    if (!items.length) { close(); return; }
    box.innerHTML = '';
    items.forEach(function (it, i) {
      var a = document.createElement('a');
      a.className = 'suggest__item';
      a.href = ctx + '/product/' + it.id;
      var name = document.createElement('span');
      name.className = 'suggest__name';
      name.textContent = it.name;
      var meta = document.createElement('span');
      meta.className = 'suggest__meta';
      meta.textContent = it.category + ' · ' + it.price;
      a.appendChild(name);
      a.appendChild(meta);
      a.addEventListener('mouseenter', function () { active = i; highlight(); });
      box.appendChild(a);
    });
    box.style.display = 'block';
    active = -1;
  }

  function highlight() {
    var nodes = box.querySelectorAll('.suggest__item');
    for (var i = 0; i < nodes.length; i++) {
      nodes[i].classList.toggle('is-active', i === active);
    }
  }

  function load(q) {
    fetch(url + '?q=' + encodeURIComponent(q))
      .then(function (r) { return r.ok ? r.json() : []; })
      .then(render)
      .catch(function () { close(); });
  }

  input.addEventListener('input', function () {
    var q = input.value.trim();
    if (q.length < 2) { close(); return; }
    if (q === lastQuery) return;
    lastQuery = q;
    clearTimeout(timer);
    timer = setTimeout(function () { load(q); }, 160);
  });

  input.addEventListener('keydown', function (e) {
    var n = items.length;
    if (box.style.display !== 'block' || !n) return;
    if (e.key === 'ArrowDown') { e.preventDefault(); active = (active + 1) % n; highlight(); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); active = (active - 1 + n) % n; highlight(); }
    else if (e.key === 'Enter' && active >= 0) { e.preventDefault(); window.location.href = ctx + '/product/' + items[active].id; }
    else if (e.key === 'Escape') { close(); }
  });

  document.addEventListener('click', function (e) {
    if (e.target !== input && !box.contains(e.target)) close();
  });
  input.addEventListener('blur', function () { setTimeout(close, 150); });
})();
