<template id="sleep-summary">
  <app-layout>

    <div class="card bg-light mb-3">
      <div class="card-header">
        <div class="row">
          <div class="col-6">Sleep</div>
          <div class="col" align="right">
            <button class="btn btn-info btn-simple btn-link" @click="hideForm = !hideForm">
              <i class="fa fa-plus"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="card-body" :class="{ 'd-none': hideForm }">
        <form>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">Hours</span>
            </div>
            <input type="number" class="form-control" v-model="formData.duration" />
          </div>

          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">Quality</span>
            </div>
            <input type="number" class="form-control" v-model="formData.quality" />
          </div>

          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">Date</span>
            </div>
            <input type="text" class="form-control" v-model="formData.date" />
          </div>
        </form>

        <button class="btn btn-info btn-simple btn-link" @click="addSleep">Add Sleep</button>
      </div>
    </div>

    <div class="list-group list-group-flush">
      <div class="list-group-item d-flex align-items-start"
           v-for="(s,index) in sleeps" :key="index">

        <div class="mr-auto p-2">
          {{ s.date }} — {{ s.duration }}h (quality {{ s.quality }})
        </div>

        <div class="p2">
          <button class="btn btn-info btn-simple btn-link" @click="editSleep(s)">
            <i class="fa fa-pencil"></i>
          </button>

          <button class="btn btn-info btn-simple btn-link" @click="deleteSleep(s.id, index)">
            <i class="fas fa-trash"></i>
          </button>
        </div>

      </div>
    </div>

  </app-layout>
</template>

<script>
app.component("sleep-summary", {
  template: "#sleep-summary",
  data: () => ({
    sleeps: [],
    formData: [],
    hideForm: true,
    editingId: null
  }),

  created() {
    const id = this.$javalin.pathParams["user-id"];
    axios.get(`/api/users/${id}/sleep`)
        .then(res => this.sleeps = res.data);
  },

  methods: {
    addSleep() {
      const id = this.$javalin.pathParams["user-id"];
      axios.post(`/api/users/${id}/sleep`, {
        duration: Number(this.formData.duration),
        quality: Number(this.formData.quality),
        date: this.formData.date,
        userId: id
      })
          .then(res => {
            this.sleeps.push(res.data);
            this.hideForm = true;
          });
    },

    editSleep(s) {
      this.editingId = s.id;
      this.formData.duration = s.duration;
      this.formData.quality = s.quality;
      this.formData.date = s.date;
      this.hideForm = false;
    },

    deleteSleep(id, index) {
      axios.delete(`/api/sleep/${id}`)
          .then(() => this.sleeps.splice(index, 1));
    }
  }
});
</script>
